package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.ReviewRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.service.ReviewService;
import lombok.RequiredArgsConstructor;
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
    public ReviewResponse postReview(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody ReviewRequest request
    ) {
        return reviewService.postReview(boardPrincipal, request, tutorId);
    }

}
