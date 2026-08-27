package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_goal_student",
                        columnNames = {"student_id", "goal_id"}
                )
        }
)
@Entity
public class GoalStudent extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentAccount studentAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private LessonGoal lessonGoal;

    private GoalStudent(StudentAccount studentAccount, LessonGoal lessonGoal) {
        this.studentAccount = studentAccount;
        this.lessonGoal = lessonGoal;
    }

    protected GoalStudent() {}

    public static GoalStudent of(StudentAccount studentAccount, LessonGoal lessonGoal) {
        return new GoalStudent(studentAccount, lessonGoal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoalStudent that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
