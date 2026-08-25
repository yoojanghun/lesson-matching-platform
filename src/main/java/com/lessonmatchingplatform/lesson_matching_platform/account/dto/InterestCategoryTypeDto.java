package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.InterestCategory;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;

public record InterestCategoryTypeDto(
        Long interestId,
        CategoryType instrument
) {

    public static InterestCategoryTypeDto of(InterestCategory interestCategory) {
        return new InterestCategoryTypeDto(
                interestCategory.getInterestId(),
                interestCategory.getName()
        );
    }
}
