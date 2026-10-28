package com.lessonmatchingplatform.lesson_matching_platform.lesson.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 리뷰 작성/삭제 커밋 직후 Redis 캐시를 동기로 무효화하는 리스너.
 *
 * ── 왜 @Async를 제거하고 동기로 처리하는가 ─────────────────────────────────────
 * @Async를 적용하면 커밋 완료 후 별도 스레드에서 evict가 비동기로 실행됩니다.
 * 이 경우 클라이언트가 리뷰 작성 API 응답을 받고 밀리초(ms) 단위로 즉시 목록을 재조회하면,
 * evict가 완료되기 전 옛 캐시 데이터를 읽어 "방금 쓴 리뷰가 안 보이는" 문제가 발생합니다.
 * Redis evict는 O(1)의 매우 가벼운(1ms 미만) 작업이므로, AFTER_COMMIT 동기 리스너로
 * 확실하게 지운 후 클라이언트 응답 흐름을 이어가는 것이 일관성을 보장합니다.
 *
 * ── 무효화 대상 ────────────────────────────────────────────────────────────
 *   1. tutorReviews::tutorId  (리뷰 첫 페이지: page 0, size 5)
 *   2. tutorDetail::tutorId   (프로필: averageRating, reviewCount 포함)
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewCacheEvictListener {

    private static final String CACHE_TUTOR_REVIEWS = "tutorReviews";
    private static final String CACHE_TUTOR_DETAIL  = "tutorDetail";

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCacheEvict(ReviewCacheEvictEvent event) {
        Long tutorId = event.tutorId();
        log.info("[ReviewCacheEvict] AFTER_COMMIT 캐시 무효화 시작 (동기 실행) | tutorId={}", tutorId);

        // 1. 리뷰 첫 페이지 캐시 무효화 (key = tutorId)
        evictSafely(CACHE_TUTOR_REVIEWS, tutorId, "리뷰 목록");

        // 2. 튜터 프로필 캐시 무효화 (averageRating, reviewCount)
        evictSafely(CACHE_TUTOR_DETAIL, tutorId, "프로필");

        log.info("[ReviewCacheEvict] 캐시 무효화 완료 | tutorId={}", tutorId);
    }

    private void evictSafely(String cacheName, Object key, String label) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                log.warn("[ReviewCacheEvict] 캐시 저장소 없음 | cacheName={}", cacheName);
                return;
            }
            cache.evict(key);
            log.debug("[ReviewCacheEvict] {} 캐시 evict 성공 | key={}", label, key);
        } catch (Exception e) {
            log.warn("[ReviewCacheEvict] {} 캐시 evict 실패 (무시) | key={} | error={}",
                    label, key, e.getMessage());
        }
    }
}
