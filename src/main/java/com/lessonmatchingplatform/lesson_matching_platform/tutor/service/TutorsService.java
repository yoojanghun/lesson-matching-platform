package com.lessonmatchingplatform.lesson_matching_platform.tutor.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class TutorsService {

    private final TutorsRepository tutorsRepository;
    private final ReviewRepository reviewRepository;

    // 공개 강사 상세 프로필 조회 (리뷰 제외) - Redis 캐싱 적용
//    @Cacheable(value = "tutorDetail", key = "#tutorId")
//    @Transactional(readOnly = true)
//    public TutorProfileResponse getTutorProfile(Long tutorId) {
//        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
//                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));
//
//        return TutorProfileResponse.from(tutorAccount);
//    }

    @Transactional(readOnly = true)
    public Page<TutorsResponse> getTutorsList(TutorSearchCondition tutorSearchCondition, Pageable pageable) {
        return tutorsRepository.searchTutors(tutorSearchCondition, pageable)
                .map(TutorsResponse::from);
    }

    @Transactional(readOnly = true)
    public TutorWithReviewsResponse getTutorAndReviews(Long tutorId, Pageable pageable) {
        TutorAccount tutorAccount = tutorsRepository.searchTutor(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        Slice<ReviewResponse> reviewResponseSlice = reviewRepository.findReviewsByTutorId(tutorId, pageable);

        return TutorWithReviewsResponse.from(tutorAccount, reviewResponseSlice);
    }

    // 카테고리 별로 캐싱된 인기 강사 10명 조회
//    @Transactional(readOnly = true)
//    public List<TutorsResponse> getPopuarTutors(Long categoryId) {
//
//    }
}
