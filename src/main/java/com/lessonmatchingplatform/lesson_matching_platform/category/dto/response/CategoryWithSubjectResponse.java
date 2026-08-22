package com.lessonmatchingplatform.lesson_matching_platform.category.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;

import java.util.List;
import java.util.stream.Collectors;

public record CategoryWithSubjectResponse(
        Long categoryId,
        String categoryName,
        String description,
        String icon,
        List<SubjectResponse> subjects              // Response로 순환참조 방지
) {

    public static CategoryWithSubjectResponse from(Category entity) {

        List<SubjectResponse> subjects = entity.getSubjects().stream()
                .map(SubjectResponse::from)
                .collect(Collectors.toList());

        CategoryType categoryType = entity.getName();

        return new CategoryWithSubjectResponse(
                entity.getCategoryId(),
                categoryType.name(),
                categoryType.getDescription(),
                categoryType.getIcon(),
                subjects
        );
    }
}
