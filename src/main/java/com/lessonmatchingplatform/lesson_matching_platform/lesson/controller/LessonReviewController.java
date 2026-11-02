package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.ReviewRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/tutors")
@RestController
public class LessonReviewController {

    private final ReviewService reviewService;

    // 한 선생님의 레슨 페이지에 리뷰 추가
    // 동시성 벤치마크를 위해 strategy 파라미터 지원 (none | pessimistic | atomic)
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{tutorId}/reviews")
    public ResponseEntity<ReviewResponse> postReview(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestParam(value = "strategy", required = false, defaultValue = "none") String strategy,
            @RequestParam(value = "useAtomic", required = false, defaultValue = "false") boolean useAtomic,
            @RequestParam(value = "usePessimistic", required = false, defaultValue = "false") boolean usePessimistic,
            @RequestBody ReviewRequest request
    ) {
        Long id = boardPrincipal.id();

        ReviewResponse reviewResponse;
        if (useAtomic || "atomic".equalsIgnoreCase(strategy)) {
            // [Case B] 조건부 / 원자적 UPDATE (Atomic SQL)
            reviewResponse = reviewService.postReviewWithAtomicUpdate(id, request, tutorId);
        } else if (usePessimistic || "pessimistic".equalsIgnoreCase(strategy)) {
            // [Case A] 비관적 락 (SELECT FOR UPDATE)
            reviewResponse = reviewService.postReviewWithPessimisticLock(id, request, tutorId);
        } else {
            // [Baseline] 동시성 제어 없음 (Lost Update 발생 확인용)
            reviewResponse = reviewService.postReview(id, request, tutorId);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponse);
    }

    // 내가 단 리뷰 삭제
    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{tutorId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @PathVariable Long reviewId
    ) {
        Long id = boardPrincipal.id();
        reviewService.deleteReview(id, tutorId, reviewId);

        return ResponseEntity.noContent().build();
    }

}
