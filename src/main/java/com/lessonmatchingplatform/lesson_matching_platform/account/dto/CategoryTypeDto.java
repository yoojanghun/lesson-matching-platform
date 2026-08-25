package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;

public record CategoryTypeDto(
        Long categoryId,
        CategoryType categoryType
) {
    public static CategoryTypeDto from(Category category) {
        return new CategoryTypeDto(
                category.getCategoryId(),
                category.getName()
        );
    }
}
