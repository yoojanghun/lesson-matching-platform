package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;

public record LessonStatusRequest(
        MatchingStatus status
) {

}
