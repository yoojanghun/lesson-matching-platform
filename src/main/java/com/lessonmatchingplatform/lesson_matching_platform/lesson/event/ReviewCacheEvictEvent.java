package com.lessonmatchingplatform.lesson_matching_platform.lesson.event;

/**
 * 리뷰 작성/삭제 후 Redis 캐시 무효화를 AFTER_COMMIT 시점에 실행하기 위한 이벤트.
 *
 * [발행 시점]
 *   ReviewService.postReview()   → 리뷰 작성 트랜잭션 커밋 직전
 *   ReviewService.deleteReview() → 리뷰 삭제 트랜잭션 커밋 직전
 *
 * [무효화 대상]
 *   1. tutorReviews::tutorId  (리뷰 첫 페이지 page 0, size 5 캐시)
 *   2. tutorDetail::tutorId   (프로필 캐시 - averageRating, reviewCount 포함)
 *
 * @param tutorId 캐시를 무효화할 튜터 ID
 */
public record ReviewCacheEvictEvent(
        Long tutorId
) {
    public static ReviewCacheEvictEvent of(Long tutorId) {
        return new ReviewCacheEvictEvent(tutorId);
    }
}
