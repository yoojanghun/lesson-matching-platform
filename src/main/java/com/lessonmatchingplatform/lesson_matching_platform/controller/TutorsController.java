package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.service.TutorsService;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/tutors")
@RequiredArgsConstructor
@RestController
public class TutorsController {

    private final TutorsService tutorsService;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public Page<TutorsResponse> getTutorsList(
            TutorSearchCondition tutorSearchCondition,
            Pageable pageable
    ) {
        return tutorsService.getTutorsList(tutorSearchCondition, pageable);
    }

    // 선생님의 레슨정보와 학생들의 리뷰(일부만)를 한꺼번에 보여주기
    @GetMapping("/{tutorId}")
    public TutorWithReviewsResponse getTutorAndReviews(
            @PathVariable Long tutorId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return tutorsService.getTutorAndReviews(tutorId, pageable);
    }

    // 선생님의 레슨 페이지에서 리뷰 더보기 버튼 클릭시 더 많은 리뷰 보여주기
    @GetMapping("/{tutorId}/reviews")
    public Slice<ReviewResponse> getReviews(
            @PathVariable Long tutorId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return reviewRepository.findReviewsByTutorId(tutorId, pageable);
    }
}
