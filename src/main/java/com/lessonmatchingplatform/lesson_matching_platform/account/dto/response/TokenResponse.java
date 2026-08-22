package com.lessonmatchingplatform.lesson_matching_platform.account.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static TokenResponse of(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
