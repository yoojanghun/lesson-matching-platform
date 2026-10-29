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
 * [핵심 방어 전략]
 * 1. 1차 캐시 확인 (Cache Hit) -> 락 없이 즉시 반환 (부하 0, 지연 0)
 * 2. 캐시 미스 시 Redisson 분산 락 획득 시도 (최대 3초 대기, Watchdog 자동 갱신)
 *    - 락 획득 실패/타임아웃 시 -> 즉시 DB 직접 조회(loader)로 안전하게 폴백
 * 3. 락 획득 후 2차 재확인 (Double-Checked Locking)
 *    - 앞선 스레드가 이미 DB에서 읽어 캐시에 저장했다면 즉시 반환
 * 4. 여전히 캐시 미스인 경우에만 loader(DB 조회) 1회 실행 후 락 내부에서 safePut
 * 5. 예외 격리:
 *    - 로더가 던진 예외(404 EntityNotFoundException 등)는 잡지 않고 호출자에게 그대로 전파 (불필요한 재실행 방지)
 *    - Cache 읽기/저장/락 해제 중 Redis 장애가 발생해도 safeGet/safePut/safeUnlock으로 삼켜 메인 비즈니스 영향 차단
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

        // 2. 분산 락 획득 시도 (예외는 락 단계에서만 격리)
        RLock lock = redissonClient.getLock("lock:" + cacheName + ":" + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[CacheLock] 락 획득 대기 중 인터럽트: key={}", key);
        } catch (Exception e) {
            log.warn("[CacheLock] 락 시도 실패(Redis 장애 등), DB 폴백: key={}, error={}", key, e.getMessage());
        }

        if (!locked) {
            log.warn("[CacheLock] 락 미획득(대기 초과), DB 폴백: key={}", key);
            return loader.get(); // 로더 예외는 그대로 호출자에게 전파
        }

        // 3. 락 획득 후 임계 영역 실행
        try {
            // 2차 재확인 (Double-Checked Locking)
            cached = safeGet(cache, key, type);
            if (cached != null) {
                return cached;
            }

            // 로더 실행 (비즈니스 예외는 그대로 전파되며, 재실행되지 않음)
            T loaded = loader.get();
            if (loaded != null) {
                safePut(cache, key, loaded);
            }
            return loaded;
        } finally {
            safeUnlock(lock);
        }
    }

    private <T> T safeGet(Cache cache, Object key, Class<T> type) {
        try {
            return cache.get(key, type);
        } catch (Exception e) {
            log.warn("[CacheLock] 캐시 읽기 실패: {}", e.getMessage());
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
