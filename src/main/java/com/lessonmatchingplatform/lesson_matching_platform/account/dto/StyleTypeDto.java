package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorStyle;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.StyleType;

public record StyleTypeDto(
        Long id,
        StyleType styleType,
        String description
) {
    public StyleTypeDto(Long id, StyleType styleType) {
        this(id, styleType, styleType != null ? styleType.getDescription() : null);
    }

    public static StyleTypeDto of(TutorStyle tutorStyle) {
        return new StyleTypeDto(
                tutorStyle.getStyleId(),
                tutorStyle.getStyleType(),
                tutorStyle.getStyleType().getDescription()
        );
    }
}
