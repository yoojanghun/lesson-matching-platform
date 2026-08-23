package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

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

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentAccount studentAccount;

    @Column(nullable = false, length = 150)
    private String goal;

    private LessonGoal (StudentAccount studentAccount, String goal) {
        this.studentAccount = studentAccount;
        this.goal = goal;
    }

    protected LessonGoal() {}

    public static LessonGoal of (StudentAccount studentAccount, String goal) {
        return new LessonGoal(studentAccount, goal);
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
