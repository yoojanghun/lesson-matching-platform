package com.lessonmatchingplatform.lesson_matching_platform.category.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;
import jakarta.validation.constraints.NotNull;

public record SubjectCreateRequest(

        @NotNull(message = "categoryId는 필수입니다.")
        Long categoryId,

        @NotNull(message = "Subject name은 필수입니다.")
        SubjectType name,

        @NotNull(message = "displayOrder는 필수입니다.")
        Integer displayOrder
) {
}
