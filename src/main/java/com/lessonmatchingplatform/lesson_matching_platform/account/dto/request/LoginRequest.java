package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

public record LoginRequest(
        String username,
        String password
) {
    public static LoginRequest of(String username, String password) {
        return new LoginRequest(username, password);
    }
}
