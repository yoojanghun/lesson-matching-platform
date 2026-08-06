package com.lessonmatchingplatform.lesson_matching_platform.tutor.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorProfileUpdateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.service.TutorsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // 강사 본인 프로필 상세 조회
    @GetMapping("/me")
    public ResponseEntity<TutorProfileResponse> getMyProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        return ResponseEntity.ok(tutorsService.getMyProfile(boardPrincipal.id()));
    }

    // 강사 본인 프로필 상세 등록 및 수정
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorProfileUpdateRequest request
    ) {
        tutorsService.updateMyProfile(boardPrincipal.id(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<TutorsResponse> getTutorsList(
            @RequestBody TutorSearchCondition tutorSearchCondition,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
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
