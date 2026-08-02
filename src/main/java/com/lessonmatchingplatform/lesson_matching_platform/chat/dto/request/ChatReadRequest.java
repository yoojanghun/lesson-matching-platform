package com.lessonmatchingplatform.lesson_matching_platform.chat.dto.request;

public record ChatReadRequest(
        Long matchingId,                    // 사전 문의 시 null
        Long studentId,
        Long tutorId
) {
}
