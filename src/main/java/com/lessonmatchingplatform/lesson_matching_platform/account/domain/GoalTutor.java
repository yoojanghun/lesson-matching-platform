package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@ToString(exclude = {"tutorAccount", "lessonGoal"})
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
public class GoalTutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private LessonGoal lessonGoal;

    private GoalTutor(TutorAccount tutorAccount, LessonGoal lessonGoal) {
        this.tutorAccount = tutorAccount;
        this.lessonGoal = lessonGoal;
    }

    public static GoalTutor of(TutorAccount tutorAccount, LessonGoal lessonGoal) {
        return new GoalTutor(tutorAccount, lessonGoal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoalTutor that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
