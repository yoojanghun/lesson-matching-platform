package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.LessonReview;

import java.math.BigDecimal;

public record ReviewResponse(
        String content,
        BigDecimal rating,
        String nickName
) {

    public static ReviewResponse from(LessonReview entity) {
        String nickName = entity.getIsAnonymous() ? "익명" : entity.getCreatedBy();

        return new ReviewResponse(
                entity.getContent(),
                entity.getRating(),
                nickName
        );
    }
}
