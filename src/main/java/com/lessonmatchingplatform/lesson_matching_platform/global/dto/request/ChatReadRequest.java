package com.lessonmatchingplatform.lesson_matching_platform.global.dto.request;

public record ChatReadRequest(
        Long matchingId,                    // 사전 문의 시 null
        Long studentId,
        Long tutorId
) {
}
