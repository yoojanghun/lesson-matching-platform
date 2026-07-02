package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReviewRepositoryCustom {
    Slice<ReviewResponse> findReviewsByTutorId(Long tutorId, Pageable pageable);
}
