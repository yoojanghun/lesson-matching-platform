package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;

public record CategoryResponse(
        Long id,
        String name
) {

    public static CategoryResponse from(Category entity) {
        return new CategoryResponse(
                entity.getCategoryId(),
                entity.getName()
        );
    }
}
