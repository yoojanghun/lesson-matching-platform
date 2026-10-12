package com.lessonmatchingplatform.lesson_matching_platform.global.reference.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.StyleTypeDto;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record ReferenceAllResponse(
        List<LocationDto> locations,
        List<CategoryTypeDto> categories,
        List<StyleTypeDto> tutorStyles,
        List<GoalTypeDto> lessonGoals,
        List<EnumReferenceDto> lessonTypes,
        List<EnumReferenceDto> sortTypes
) {
    public static ReferenceAllResponse of(
            List<LocationDto> locations,
            List<CategoryTypeDto> categories,
            List<StyleTypeDto> tutorStyles,
            List<GoalTypeDto> lessonGoals,
            List<EnumReferenceDto> lessonTypes,
            List<EnumReferenceDto> sortTypes
    ) {
        return new ReferenceAllResponse(locations, categories, tutorStyles, lessonGoals, lessonTypes, sortTypes);
    }
}
