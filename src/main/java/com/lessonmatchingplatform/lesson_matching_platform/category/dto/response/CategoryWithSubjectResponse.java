package com.lessonmatchingplatform.lesson_matching_platform.category.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;

import java.util.List;

public record CategoryWithSubjectResponse(
        Long categoryId,
        CategoryType name,
        List<SubjectResponse> subjects              // Response로 순환참조 방지
) {

    public static CategoryWithSubjectResponse from(Category entity) {

        List<SubjectResponse> subjects = entity.getSubjects().stream()
                .map(SubjectResponse::from)
                .toList();

        return new CategoryWithSubjectResponse(
                entity.getCategoryId(),
                entity.getName(),
                subjects
        );
    }
}
