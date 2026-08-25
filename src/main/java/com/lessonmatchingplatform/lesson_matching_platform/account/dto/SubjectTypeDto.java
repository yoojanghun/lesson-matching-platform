package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;

public record SubjectTypeDto(
        Long subjectId,
        SubjectType subjectType
) {
    public static SubjectTypeDto from(Subject subject) {
        return new SubjectTypeDto(
                subject.getSubjectId(),
                subject.getName()
        );
    }
}
