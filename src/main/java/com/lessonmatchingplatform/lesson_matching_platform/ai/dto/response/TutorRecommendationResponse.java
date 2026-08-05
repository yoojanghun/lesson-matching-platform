package com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response;

public record TutorRecommendationResponse(
        Long tutorId,
        String tutorName,
        String recommendationReason
) {
}
