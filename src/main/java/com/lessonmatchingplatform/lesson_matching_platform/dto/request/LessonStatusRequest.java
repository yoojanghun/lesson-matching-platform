package com.lessonmatchingplatform.lesson_matching_platform.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;

public record LessonStatusRequest(
        MatchingStatus status
) {

}
