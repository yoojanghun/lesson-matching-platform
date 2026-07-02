package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import java.util.Set;

public record TutorSwitchRequest(
        String introduction,
        String career,
        String title,
        String content,
        Set<Long> categoryId,
        Set<Long> subjectId,
        Set<Long> locationId
) {
}
