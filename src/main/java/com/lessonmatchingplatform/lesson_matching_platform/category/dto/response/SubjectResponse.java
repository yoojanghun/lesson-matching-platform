package com.lessonmatchingplatform.lesson_matching_platform.category.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;

public record SubjectResponse(
        Long subjectId,
        String subjectName,
        String description
) {

    public static SubjectResponse from(Subject entity) {
        SubjectType subjectType = entity.getName();

        return new SubjectResponse(
                entity.getSubjectId(),
                subjectType.name(),
                subjectType.getDescription()
        );
    }
}
