package com.lessonmatchingplatform.lesson_matching_platform.category.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;

public record SubjectResponse(
        Long subjectId,
        SubjectType name
) {

    public static SubjectResponse from(Subject entity) {
        return new SubjectResponse(
                entity.getSubjectId(),
                entity.getName()
        );
    }
}
