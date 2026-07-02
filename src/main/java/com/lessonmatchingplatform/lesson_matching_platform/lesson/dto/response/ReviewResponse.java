package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.LessonReview;

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
