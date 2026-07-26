package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PricePerLessonRequest(

        @NotNull(message = "회당 레슨비는 필수 항목입니다.")
        @PositiveOrZero(message = "레슨비는 0원 이상이어야 합니다.")
        Integer pricePerLesson
) {
}
