package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;
import com.lessonmatchingplatform.lesson_matching_platform.type.CategoryType;

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
