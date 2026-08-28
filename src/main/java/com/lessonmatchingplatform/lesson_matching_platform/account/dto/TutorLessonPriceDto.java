package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorLessonPrice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TutorLessonPriceDto(
        @NotBlank(message = "클래스 이름을 입력해주세요.")
        String className,
        
        @NotNull(message = "가격을 입력해주세요.")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price
) {
    public static TutorLessonPriceDto from(TutorLessonPrice entity) {
        return new TutorLessonPriceDto(
                entity.getClassName(),
                entity.getPrice()
        );
    }
}
