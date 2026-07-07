package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

public record LoginRequest(
        String username,
        String password
) {}
