package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Subject;

public record SubjectResponse(
        Long subjectId,
        String name
) {

    public static SubjectResponse from(Subject entity) {
        return new SubjectResponse(
                entity.getSubjectId(),
                entity.getName()
        );
    }
}
