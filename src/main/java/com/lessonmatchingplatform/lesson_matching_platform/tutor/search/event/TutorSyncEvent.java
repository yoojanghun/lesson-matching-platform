package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.event;

import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.type.EventType;

public record TutorSyncEvent(
        Long tutorId,
        EventType eventType         // CREATED_OR_UPDATED, DELETED
) {
}
