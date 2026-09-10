package com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;

public record TutorRecommendationResponse(
        Long tutorId,
        String tutorName,
        String recommendationReason,
        TutorProfileResponse profile
) {
    public static TutorRecommendationResponse of(Long tutorId, String tutorName, String recommendationReason, TutorProfileResponse profile) {
        return new TutorRecommendationResponse(tutorId, tutorName, recommendationReason, profile);
    }
}
