package com.lessonmatchingplatform.lesson_matching_platform.category.dto;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryType;

public record CategoryResponse(
        Long id,
        CategoryType name,
        String displayName,
        String icon
) {

    public static CategoryResponse from(Category entity) {
        CategoryType categoryType = entity.getName();
        return new CategoryResponse(
                entity.getCategoryId(),
                categoryType,
                categoryType.getDescription(),
                categoryType.getIcon()
        );
    }
}
