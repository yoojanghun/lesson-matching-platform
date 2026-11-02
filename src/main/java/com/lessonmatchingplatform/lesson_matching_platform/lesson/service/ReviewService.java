package com.lessonmatchingplatform.lesson_matching_platform.lesson.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.LessonReview;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.ReviewRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.event.ReviewCacheEvictEvent;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.event.TutorSyncEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MatchingRepository matchingRepository;
    private final TutorsRepository tutorsRepository;
    private final TutorSyncEventPublisher tutorSyncEventPublisher;

    /**
     * 리뷰 작성/삭제 후 캐시 무효화 이벤트를 발행하기 위한 퍼블리셔.
     * @TransactionalEventListener(AFTER_COMMIT) 에서 처리되므로
     * 커밋-evict 사이 Race Condition 이 발생하지 않는다.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * [기본 방식 - 동시성 제어 없음 (Lost Update 발생 확인용)]
     */
    public ReviewResponse postReview(Long id, ReviewRequest request, Long tutorId) {
        if(matchingRepository.hasAlreadyReviewedTutor(tutorId, id)) {
            throw new IllegalStateException("이미 리뷰를 작성한 수업입니다.");
        }

        Matching matching = matchingRepository.findByStudentAccount_StudentIdAndTutorAccount_TutorIdAndStatus(
                        id,
                        tutorId,
                        MatchingStatus.ACCEPTED
                ).orElseThrow(() -> new EntityNotFoundException("해당 강사에 대해 리뷰를 작성할 수 있는 승인된 매칭이 없습니다."));

        TutorAccount tutorAccount = tutorsRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("강사를 찾을 수 없습니다."));

        tutorAccount.updateRating(request.rating());

        LessonReview lessonReview = LessonReview.of(matching, request.content(), request.rating(), request.isAnonymous());
        ReviewResponse response = ReviewResponse.from(reviewRepository.save(lessonReview));

        // ES 도큐먼트(totalScore) 동기화
        tutorSyncEventPublisher.publishSaveEvent(tutorId);

        // 캐시 무효화 이벤트 발행
        eventPublisher.publishEvent(ReviewCacheEvictEvent.of(tutorId));

        return response;
    }

    /**
     * [벤치마크 Case A: 비관적 락 (SELECT FOR UPDATE)]
     * TutorAccount 행에 배타적 락(X-Lock)을 걸어 평점 갱신 트랜잭션을 직렬화.
     */
    public ReviewResponse postReviewWithPessimisticLock(Long id, ReviewRequest request, Long tutorId) {
        if(matchingRepository.hasAlreadyReviewedTutor(tutorId, id)) {
            throw new IllegalStateException("이미 리뷰를 작성한 수업입니다.");
        }

        Matching matching = matchingRepository.findByStudentAccount_StudentIdAndTutorAccount_TutorIdAndStatus(
                        id,
                        tutorId,
                        MatchingStatus.ACCEPTED
                ).orElseThrow(() -> new EntityNotFoundException("해당 강사에 대해 리뷰를 작성할 수 있는 승인된 매칭이 없습니다."));

        // 비관적 락(FOR UPDATE)으로 튜터 행 잠금
        TutorAccount tutorAccount = tutorsRepository.findByIdWithLock(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("강사를 찾을 수 없습니다."));

        tutorAccount.updateRating(request.rating());

        LessonReview lessonReview = LessonReview.of(matching, request.content(), request.rating(), request.isAnonymous());
        ReviewResponse response = ReviewResponse.from(reviewRepository.save(lessonReview));

        // ES 도큐먼트(totalScore) 동기화
        tutorSyncEventPublisher.publishSaveEvent(tutorId);

        // 캐시 무효화 이벤트 발행
        eventPublisher.publishEvent(ReviewCacheEvictEvent.of(tutorId));

        return response;
    }

    /**
     * [벤치마크 Case B: 조건부/원자적 UPDATE (Atomic SQL Update)]
     * 락 없이 단 1회의 원자적 UPDATE 쿼리로 reviewCount와 averageRating을 갱신.
     */
    public ReviewResponse postReviewWithAtomicUpdate(Long id, ReviewRequest request, Long tutorId) {
        if(matchingRepository.hasAlreadyReviewedTutor(tutorId, id)) {
            throw new IllegalStateException("이미 리뷰를 작성한 수업입니다.");
        }

        Matching matching = matchingRepository.findByStudentAccount_StudentIdAndTutorAccount_TutorIdAndStatus(
                        id,
                        tutorId,
                        MatchingStatus.ACCEPTED
                ).orElseThrow(() -> new EntityNotFoundException("해당 강사에 대해 리뷰를 작성할 수 있는 승인된 매칭이 없습니다."));

        // 1. 단일 원자적 SQL UPDATE로 평점 및 리뷰 수 갱신 (락 대기/엔티티 dirty checking 생략)
        int updated = tutorsRepository.updateRatingAtomically(tutorId, request.rating());
        if (updated == 0) {
            throw new EntityNotFoundException("강사를 찾을 수 없습니다.");
        }

        // 2. 리뷰 엔티티 저장
        LessonReview lessonReview = LessonReview.of(matching, request.content(), request.rating(), request.isAnonymous());
        ReviewResponse response = ReviewResponse.from(reviewRepository.save(lessonReview));

        // ES 도큐먼트(totalScore) 동기화
        tutorSyncEventPublisher.publishSaveEvent(tutorId);

        // 캐시 무효화 이벤트 발행
        eventPublisher.publishEvent(ReviewCacheEvictEvent.of(tutorId));

        return response;
    }

    public void deleteReview(Long id, Long tutorId, Long reviewId) {
        LessonReview lessonReview = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 review를 찾을 수 없습니다. id = " + reviewId));

        TutorAccount tutorAccount = lessonReview.getMatching().getTutorAccount();
        StudentAccount studentAccount = lessonReview.getMatching().getStudentAccount();

        if (!studentAccount.getStudentId().equals(id) && !tutorAccount.getTutorId().equals(tutorId)) {
            throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다");
        }

        tutorAccount.deleteReview(lessonReview.getRating());
        reviewRepository.delete(lessonReview);

        // ES 도큐먼트 동기화: 기존 로직 유지
        tutorSyncEventPublisher.publishDeleteEvent(tutorId);

        // 캐시 무효화 이벤트 발행 (AFTER_COMMIT 에서 처리)
        eventPublisher.publishEvent(ReviewCacheEvictEvent.of(tutorId));
    }
}
