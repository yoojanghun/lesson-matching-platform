package com.lessonmatchingplatform.lesson_matching_platform.tutor.controller;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.service.TutorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/tutors")
@RequiredArgsConstructor
@RestController
public class TutorsController {

    private final TutorsService tutorsService;
    private final ReviewRepository reviewRepository;

    // 강사 상세 프로필 조회 (리뷰 제외, Redis 캐싱 적용)
    @GetMapping("/{tutorId}/profile")
    public ResponseEntity<TutorProfileResponse> getTutorProfile(
            @PathVariable Long tutorId
    ) {
        return ResponseEntity.ok(tutorsService.getTutorProfile(tutorId));
    }

    @GetMapping
    public ResponseEntity<Page<TutorCardDto>> getTutorsList(
            @RequestBody TutorSearchCondition tutorSearchCondition,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TutorCardDto> tutorCardDtoPage = tutorsService.getTutorsList(tutorSearchCondition, pageable);
        return ResponseEntity.ok(tutorCardDtoPage);
    }

    // 선생님의 레슨 페이지에서 리뷰 보여주기
    @GetMapping("/{tutorId}/reviews")
    public ResponseEntity<Slice<ReviewResponse>> getReviews(
            @PathVariable Long tutorId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<ReviewResponse> reviewResponseSlice = tutorsService.findReviewsByTutorId(tutorId, pageable);

        return ResponseEntity.ok(reviewResponseSlice);
    }
}
