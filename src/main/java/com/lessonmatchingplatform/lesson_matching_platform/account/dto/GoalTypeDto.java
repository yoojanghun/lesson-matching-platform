package com.lessonmatchingplatform.lesson_matching_platform.account.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.LessonGoal;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonGoalType;

public record GoalTypeDto(
        Long goalId,
        LessonGoalType lessonGoalType
) {

    public static GoalTypeDto of(LessonGoal lessonGoal) {
        return new GoalTypeDto(
                lessonGoal.getGoalId(),
                lessonGoal.getLessonGoalType()
        );
    }
}
