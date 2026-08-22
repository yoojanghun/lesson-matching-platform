package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
    public static AuthTokens of(String accessToken, String refreshToken, Long expiresIn) {
        return new AuthTokens(accessToken, refreshToken, expiresIn);
    }
}
