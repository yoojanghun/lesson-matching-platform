package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.LessonReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        String content,
        BigDecimal rating,
        String nickName,
        LocalDateTime createdAt
) {

    public static ReviewResponse from(LessonReview entity) {
        String nickName = entity.getIsAnonymous() ? "익명" : entity.getCreatedBy();

        return new ReviewResponse(
                entity.getCommentId(),
                entity.getContent(),
                entity.getRating(),
                nickName,
                entity.getCreatedAt()
        );
    }
}
