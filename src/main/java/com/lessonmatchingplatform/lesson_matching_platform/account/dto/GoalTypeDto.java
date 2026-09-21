package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.LessonGoal;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonGoalType;

public record GoalTypeDto(
        Long goalId,
        LessonGoalType lessonGoalType,
        String description
) {

    public GoalTypeDto(Long goalId, LessonGoalType lessonGoalType) {
        this(goalId, lessonGoalType, lessonGoalType != null ? lessonGoalType.getDescription() : null);
    }

    public static GoalTypeDto of(LessonGoal lessonGoal) {
        return new GoalTypeDto(
                lessonGoal.getGoalId(),
                lessonGoal.getLessonGoalType(),
                lessonGoal.getLessonGoalType().getDescription()
        );
    }
}
