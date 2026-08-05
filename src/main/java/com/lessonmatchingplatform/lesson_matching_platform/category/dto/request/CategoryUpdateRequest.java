package com.lessonmatchingplatform.lesson_matching_platform.category.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import jakarta.validation.constraints.NotNull;

public record CategoryUpdateRequest(

        @NotNull(message = "카테고리명은 필수 입력 값입니다.")
        CategoryType name,

        @NotNull(message = "표시 순서는 필수 입력 값입니다.")
        Integer displayOrder
) {
}
