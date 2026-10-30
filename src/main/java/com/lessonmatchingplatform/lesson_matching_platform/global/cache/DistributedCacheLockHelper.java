package com.lessonmatchingplatform.lesson_matching_platform.global.cache;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Redisson 기반 분산 락 + 논리/물리 TTL (Stale-While-Revalidate) Cache Stampede 방어 컴포넌트
 *
 * [상태별 동작]
 * 1. FRESH (soft 만료 전): 캐시 값을 그대로 즉시 반환 (HIT, 지연 0ms)
 * 2. STALE (soft 만료 후, hard 만료 전):
 *    - 옛 값을 즉시 반환 (지연 0ms)
 *    - 백그라운드 스레드풀에서 단 1개 스레드만 비동기로 DB 조회 및 캐시 갱신 (STALE)
 *    - 락 획득/해제를 모두 갱신 스레드 내부에서 처리 (Lock Thread-Binding 준수)
 *    - 갱신 실패 시 쿨다운(soft 만료 5초 연장) 적용하되, hardExpireAt은 불변 유지하여 최대 수명 초과 차단
 * 3. MISS (hard 만료 후 또는 최초 조회):
 *    - 기존 Single-Flight Spin-Wait Polling 경로로 동기 로드 (MISS)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedCacheLockHelper {

    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    // 백그라운드 갱신 작업의 JVM 내 중복 실행 차단용 Set
    private final Set<String> refreshingKeys = ConcurrentHashMap.newKeySet();

    // 백그라운드 캐시 갱신 전용 스레드풀 (큐 및 스레드 수 제한)
    private final ExecutorService refreshExecutor = new ThreadPoolExecutor(
            2,
            8,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "cache-refresh-" + count.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     * 논리 TTL (Soft TTL) 및 최대 물리 보장 수명 (Hard TTL) 적용 조회 (Stale-While-Revalidate)
     */
    public <T> T getOrLoadWithSoftTtl(String cacheName, Object key, Class<T> type,
                                      Duration softTtl, Duration hardTtl, Supplier<T> loader) {
        if (hardTtl.compareTo(softTtl) <= 0) {
            throw new IllegalArgumentException("hardTtl must be greater than softTtl");
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("[CacheLock] Cache '{}' 를 찾을 수 없어 DB 직접 조회로 폴백합니다.", cacheName);
            CacheContext.setStatus("MISS");
            return loader.get();
        }

        // 1. 캐시 조회 및 논리/물리 TTL 확인
        CachedValue<T> cv = safeGetCachedValue(cache, key, type);
        if (cv != null && cv.getData() != null) {
            // Hard TTL을 초과한 경우 즉시 만료(MISS)로 취급하여 동기 로드
            if (cv.isHardExpired()) {
                CacheContext.setStatus("MISS");
                return loadWithLockAndSoftTtl(cache, cacheName, key, type, softTtl, hardTtl, loader);
            }

            // FRESH: 소프트 만료 전 → 즉시 반환
            if (!cv.isExpired()) {
                CacheContext.setStatus("HIT");
                return cv.getData();
            }

            // STALE: 소프트 만료 후, 하드 만료 전 → 옛 값 즉시 반환하고 백그라운드 갱신 트리거
            CacheContext.setStatus("STALE");
            triggerBackgroundRefresh(cache, cacheName, key, type, softTtl, hardTtl, loader, cv);
            return cv.getData();
        }

        // MISS: 캐시 미존재 또는 역직렬화 실패 → 동기 락 획득 및 로드
        CacheContext.setStatus("MISS");
        return loadWithLockAndSoftTtl(cache, cacheName, key, type, softTtl, hardTtl, loader);
    }

    /**
     * 기존 단순 Redisson 락 조회 (논리 TTL 미적용, 하위 호환성 유지)
     */
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("[CacheLock] Cache '{}' 를 찾을 수 없어 DB 직접 조회로 폴백합니다.", cacheName);
            CacheContext.setStatus("MISS");
            return loader.get();
        }

        T cached = safeGet(cache, key, type);
        if (cached != null) {
            CacheContext.setStatus("HIT");
            return cached;
        }

        CacheContext.setStatus("MISS");
        RLock lock = redissonClient.getLock("lock:" + cacheName + ":" + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[CacheLock] 락 시도 실패(Redis 장애 등): key={}, error={}", key, e.getMessage());
        }

        if (locked) {
            try {
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

        cached = safeGet(cache, key, type);
        if (cached != null) {
            return cached;
        }

        log.warn("[CacheLock] 대기 초과, 최후의 DB 폴백: cache={}, key={}", cacheName, key);
        return loader.get();
    }

    /**
     * 백그라운드 캐시 갱신 트리거
     * - JVM 내 중복 갱신 1차 차단 (refreshingKeys)
     * - 분산 환경 중복 갱신 2차 차단 (tryLock(0))
     * - 락 획득과 해제를 반드시 갱신 스레드 내부에서 수행
     * - 실패 시 5초 쿨다운 적용하되 hardExpireAt은 유지하여 최대 수명 초과 방지
     */
    private <T> void triggerBackgroundRefresh(Cache cache, String cacheName, Object key, Class<T> type,
                                               Duration softTtl, Duration hardTtl, Supplier<T> loader, CachedValue<T> staleValue) {
        String lockKeyId = cacheName + ":" + key;

        // 1. JVM 내 중복 차단
        if (!refreshingKeys.add(lockKeyId)) {
            return;
        }

        try {
            refreshExecutor.execute(() -> {
                RLock lock = redissonClient.getLock("lock:" + lockKeyId);
                boolean locked = false;
                try {
                    // 2. 분산 환경 중복 차단 (대기 없이 즉시 시도)
                    locked = lock.tryLock(0, TimeUnit.SECONDS);
                    if (!locked) {
                        return;
                    }

                    // 락 획득 후 다른 인스턴스가 이미 갱신했는지 재확인
                    CachedValue<T> current = safeGetCachedValue(cache, key, type);
                    if (current != null && !current.isExpired()) {
                        return;
                    }

                    long start = System.currentTimeMillis();
                    T freshData = loader.get();
                    if (freshData != null) {
                        long softExpireAt = System.currentTimeMillis() + softTtl.toMillis();
                        long hardExpireAt = System.currentTimeMillis() + hardTtl.toMillis();
                        safePut(cache, key, new CachedValue<>(freshData, softExpireAt, hardExpireAt));
                        log.info("[CacheLock] 백그라운드 갱신 저장 완료 | key={} | elapsed={}ms | thread={}",
                                lockKeyId, System.currentTimeMillis() - start, Thread.currentThread().getName());
                    }
                } catch (Exception e) {
                    log.warn("[CacheLock] 백그라운드 갱신 실패, 5초 쿨다운 적용 | key={}, error={}", lockKeyId, e.getMessage());
                    // 3. 갱신 실패 쿨다운: soft 만료를 5초 연장하되, hardExpireAt은 기존 값을 유지하여 무한 연장 방지
                    if (staleValue != null && staleValue.getData() != null) {
                        long cooldownSoftExpireAt = System.currentTimeMillis() + 5_000L;
                        long hardExpireAt = staleValue.getHardExpireAt();
                        if (cooldownSoftExpireAt > hardExpireAt) {
                            cooldownSoftExpireAt = hardExpireAt;
                        }
                        safePut(cache, key, new CachedValue<>(staleValue.getData(), cooldownSoftExpireAt, hardExpireAt));
                    }
                } finally {
                    safeUnlock(lock);
                    refreshingKeys.remove(lockKeyId);
                }
            });
        } catch (RejectedExecutionException e) {
            refreshingKeys.remove(lockKeyId);
            log.warn("[CacheLock] 갱신 스레드풀 포화로 작업 스킵 | key={}", lockKeyId);
        }
    }

    /**
     * MISS 시 동기 락 로드 (Single-Flight + Spin-Wait Polling)
     */
    private <T> T loadWithLockAndSoftTtl(Cache cache, String cacheName, Object key, Class<T> type,
                                         Duration softTtl, Duration hardTtl, Supplier<T> loader) {
        RLock lock = redissonClient.getLock("lock:" + cacheName + ":" + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[CacheLock] 락 시도 실패(Redis 장애 등): key={}, error={}", key, e.getMessage());
        }

        // 승자: 동기 로드 및 캐시 저장
        if (locked) {
            try {
                CachedValue<T> cv = safeGetCachedValue(cache, key, type);
                if (cv != null && cv.getData() != null && !cv.isHardExpired()) {
                    return cv.getData();
                }

                T loaded = loader.get();
                if (loaded != null) {
                    long softExpireAt = System.currentTimeMillis() + softTtl.toMillis();
                    long hardExpireAt = System.currentTimeMillis() + hardTtl.toMillis();
                    safePut(cache, key, new CachedValue<>(loaded, softExpireAt, hardExpireAt));
                }
                return loaded;
            } finally {
                safeUnlock(lock);
            }
        }

        // 패자: 락 대기열에 줄 서지 않고 캐시가 채워질 때까지 병렬 폴링 (최대 3초)
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            CachedValue<T> cv = safeGetCachedValue(cache, key, type);
            if (cv != null && cv.getData() != null && !cv.isHardExpired()) {
                return cv.getData();
            }
        }

        // 폴백 직전 최종 확인
        CachedValue<T> cv = safeGetCachedValue(cache, key, type);
        if (cv != null && cv.getData() != null && !cv.isHardExpired()) {
            return cv.getData();
        }

        log.warn("[CacheLock] 대기 초과, 최후의 DB 폴백: cache={}, key={}", cacheName, key);
        return loader.get();
    }

    @SuppressWarnings("unchecked")
    private <T> CachedValue<T> safeGetCachedValue(Cache cache, Object key, Class<T> dataType) {
        try {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null && wrapper.get() != null) {
                Object value = wrapper.get();
                if (value instanceof CachedValue<?> cv) {
                    if (cv.getData() != null && dataType.isInstance(cv.getData())) {
                        return (CachedValue<T>) cv;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[CacheLock] CachedValue 읽기 실패: key={}, error={}", key, e.getMessage());
            return null;
        }
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

    @PreDestroy
    public void shutdown() {
        log.info("[CacheLock] refreshExecutor graceful shutdown 시작...");
        refreshExecutor.shutdown();
        try {
            if (!refreshExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                refreshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            refreshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
