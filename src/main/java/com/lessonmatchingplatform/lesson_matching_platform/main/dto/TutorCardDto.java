package com.lessonmatchingplatform.lesson_matching_platform.main.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.SubjectTypeDto;

import java.util.List;

public record TutorCardDto(
        String name,
        String title,
        List<GoalTypeDto> goalTypeDtoList,
        List<CategoryTypeDto> categoryTypeDtoList,
        List<SubjectTypeDto> subjectTypeDtoList
) {
}
