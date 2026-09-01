package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.type.TutorSortType;

import java.util.List;

public record TutorSearchCondition(
        List<Long> categoryIds,
        List<Long> subjectIds,
        List<Long> goalIds,
        List<Long> styleIds,
        List<Long> locationIds,
        LessonType lessonType,
        TutorSortType tutorSortType,
        Integer minPrice,
        Integer maxPrice
) {
}
