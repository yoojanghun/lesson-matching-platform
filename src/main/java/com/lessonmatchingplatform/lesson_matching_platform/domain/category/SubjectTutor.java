package com.lessonmatchingplatform.lesson_matching_platform.domain.category;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class SubjectTutor extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private TutorAccount tutorAccount;

    protected SubjectTutor() {}

    private SubjectTutor(Subject subject, TutorAccount tutorAccount) {
        this.subject = subject;
        this.tutorAccount = tutorAccount;
    }

    public static SubjectTutor of(Subject subject, TutorAccount tutorAccount) {
        return new SubjectTutor(subject, tutorAccount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectTutor that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
