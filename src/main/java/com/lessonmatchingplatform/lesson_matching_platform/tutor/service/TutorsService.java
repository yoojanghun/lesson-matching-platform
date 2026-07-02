package com.lessonmatchingplatform.lesson_matching_platform.tutor.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public Page<TutorsResponse> getTutorsList(TutorSearchCondition tutorSearchCondition, Pageable pageable) {
        return tutorsRepository.searchTutors(tutorSearchCondition, pageable)
                .map(TutorsResponse::from);
    }

    @Transactional(readOnly = true)
    public TutorWithReviewsResponse getTutorAndReviews(Long tutorId, Pageable pageable) {
        TutorAccount tutorAccount = tutorsRepository.searchTutor(tutorId)
                .orElseThrow(EntityNotFoundException::new);

        Slice<ReviewResponse> reviewResponseSlice = reviewRepository.findReviewsByTutorId(tutorId, pageable);

        return TutorWithReviewsResponse.from(tutorAccount, reviewResponseSlice);
    }
}
