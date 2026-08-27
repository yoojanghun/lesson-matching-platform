package com.lessonmatchingplatform.lesson_matching_platform.global.reference.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.StyleTypeDto;

import java.util.List;

public record ReferenceAllResponse(
        List<LocationDto> locations,
        List<CategoryTypeDto> categories,
        List<StyleTypeDto> tutorStyles,
        List<GoalTypeDto> lessonGoals
) {
    public static ReferenceAllResponse of(
            List<LocationDto> locations,
            List<CategoryTypeDto> categories,
            List<StyleTypeDto> tutorStyles,
            List<GoalTypeDto> lessonGoals
    ) {
        return new ReferenceAllResponse(locations, categories, tutorStyles, lessonGoals);
    }
}
