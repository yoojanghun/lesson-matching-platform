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
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{tutorId}/reviews")
    public ResponseEntity<ReviewResponse> postReview(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody ReviewRequest request
    ) {
        Long id = boardPrincipal.id();

        ReviewResponse reviewResponse = reviewService.postReview(id, request, tutorId);
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
