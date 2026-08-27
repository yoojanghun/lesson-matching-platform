package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonGoalType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class LessonGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 150, unique = true)
    private LessonGoalType lessonGoalType;

    private LessonGoal (LessonGoalType lessonGoalType) {
        this.lessonGoalType = lessonGoalType;
    }

    protected LessonGoal() {}

    public static LessonGoal of (LessonGoalType lessonGoalType) {
        return new LessonGoal(lessonGoalType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonGoal that)) return false;
        return this.goalId != null && Objects.equals(goalId, that.goalId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(goalId);
    }
}
