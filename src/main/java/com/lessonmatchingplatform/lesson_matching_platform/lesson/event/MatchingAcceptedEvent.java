package com.lessonmatchingplatform.lesson_matching_platform.lesson.event;

public record MatchingAcceptedEvent(
        Long studentId,
        String phoneNumber
) {
}
