package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.repository.TutorsRepository;
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
