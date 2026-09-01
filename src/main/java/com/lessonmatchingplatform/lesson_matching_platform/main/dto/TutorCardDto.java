package com.lessonmatchingplatform.lesson_matching_platform.main.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.SubjectTypeDto;

import java.math.BigDecimal;
import java.util.List;

public record TutorCardDto(
        Long tutorId,
        String name,
        String title,
        List<GoalTypeDto> goalTypeDtoList,
        List<CategoryTypeDto> categoryTypeDtoList,
        List<SubjectTypeDto> subjectTypeDtoList,
        TutorLessonPriceRangeDto priceRange,
        Integer reviewCount,
        BigDecimal averageRating
) {
    public static TutorCardDto of(
            TutorAccount tutorAccount,
            List<GoalTypeDto> goalTypeDtoList,
            List<CategoryTypeDto> categoryTypeDtoList,
            List<SubjectTypeDto> subjectTypeDtoList,
            TutorLessonPriceRangeDto priceRange
    ) {
        return new TutorCardDto(
                tutorAccount.getTutorId(),
                tutorAccount.getUserAccount().getName(),
                tutorAccount.getTitle(),
                goalTypeDtoList,
                categoryTypeDtoList,
                subjectTypeDtoList,
                priceRange,
                tutorAccount.getReviewCount(),
                tutorAccount.getAverageRating()
        );
    }
}
