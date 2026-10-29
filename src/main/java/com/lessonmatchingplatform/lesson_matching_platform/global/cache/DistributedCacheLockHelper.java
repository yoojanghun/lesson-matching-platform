package com.lessonmatchingplatform.lesson_matching_platform.global.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 기반 분산 락 Cache Stampede 방어 컴포넌트
 *
 * [핵심 방어 전략: Lock-Free Polling (Single-Flight + Spin-Wait)]
 * 1. 1차 캐시 확인 (Cache Hit) -> 락 없이 즉시 반환 (부하 0, 지연 0)
 * 2. 캐시 미스 시 Redisson 분산 락 즉시 시도 (tryLock(0)):
 *    - 대기 시간 0으로 설정하여 Lock Convoy(대기열 직렬화 병목)를 원천 차단
 * 3. 승자(Lock 획득자, 단 1개 스레드):
 *    - Double-Checked Locking 재확인 후 DB 조회(loader) 1회 실행 및 cache.put
 * 4. 패자(나머지 모든 대기 스레드):
 *    - 락 대기열에 줄 서지 않고, 캐시가 채워질 때까지 20ms 간격으로 병렬 폴링 (최대 3초)
 *    - 승자가 캐시를 쓰는 즉시 모든 패자가 동시에 캐시를 읽고 반환 (지연 최소화, Stampede 완전 차단)
 * 5. 폴백 직전 3차 캐시 확인 후 안전하게 DB 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedCacheLockHelper {

    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("[CacheLock] Cache '{}' 를 찾을 수 없어 DB 직접 조회로 폴백합니다.", cacheName);
            return loader.get();
        }

        // 1. 1차 확인 (Cache Hit 시 락 획득 없이 즉시 반환)
        T cached = safeGet(cache, key, type);
        if (cached != null) {
            return cached;
        }

        // 2. 락 획득 시도 (대기 시간 0: 승자만 즉시 획득, 나머지는 즉시 패자로 전환)
        RLock lock = redissonClient.getLock("lock:" + cacheName + ":" + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[CacheLock] 락 시도 실패(Redis 장애 등): key={}, error={}", key, e.getMessage());
        }

        // 3. 승자: DB 조회 후 캐시 저장
        if (locked) {
            try {
                // 2차 재확인 (Double-Checked Locking)
                cached = safeGet(cache, key, type);
                if (cached != null) {
                    return cached;
                }

                T loaded = loader.get();
                if (loaded != null) {
                    safePut(cache, key, loaded);
                }
                return loaded;
            } finally {
                safeUnlock(lock);
            }
        }

        // 4. 패자: 락 대기열에 줄 서지 않고, 캐시가 채워질 때까지 병렬 폴링 (최대 3초)
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            cached = safeGet(cache, key, type);
            if (cached != null) {
                return cached;
            }
        }

        // 5. 폴백 직전 최종 확인
        cached = safeGet(cache, key, type);
        if (cached != null) {
            return cached;
        }

        log.warn("[CacheLock] 대기 초과, 최후의 DB 폴백: cache={}, key={}", cacheName, key);
        return loader.get();
    }

    @SuppressWarnings("unchecked")
    private <T> T safeGet(Cache cache, Object key, Class<T> type) {
        try {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null && wrapper.get() != null) {
                Object value = wrapper.get();
                if (type.isInstance(value)) {
                    return (T) value;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[CacheLock] 캐시 읽기 실패: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    private void safePut(Cache cache, Object key, Object value) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            log.warn("[CacheLock] 캐시 저장 실패: {}", e.getMessage());
        }
    }

    private void safeUnlock(RLock lock) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("[CacheLock] 락 해제 실패: {}", e.getMessage());
        }
    }
}
