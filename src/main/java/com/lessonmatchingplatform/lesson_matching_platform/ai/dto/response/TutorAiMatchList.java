package com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response;

import java.util.List;

public record TutorAiMatchList(
        List<TutorAiMatch> matches
) {
}
