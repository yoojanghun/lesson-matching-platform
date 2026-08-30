package com.lessonmatchingplatform.lesson_matching_platform.tutor.controller;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.service.TutorsService;
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

    // 강사 상세 프로필 조회 (리뷰 제외, Redis 캐싱 적용)
//    @GetMapping("/{tutorId}/profile")
//    public ResponseEntity<TutorProfileResponse> getTutorProfile(
//            @PathVariable Long tutorId
//    ) {
//        return ResponseEntity.ok(tutorsService.getTutorProfile(tutorId));
//    }

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

    // 카테고리 별로 캐싱된 인기 강사 10명 조회
//    @GetMapping("/popular")
//    public ResponseEntity<List<TutorsResponse>> getPopularTutors(
//            @RequestParam Long categoryId
//    ) {
//        List<TutorsResponse> popularTutors = tutorsService.getPopuarTutors(categoryId);
//
//        return ResponseEntity.ok().body(popularTutors);
//    }
}
