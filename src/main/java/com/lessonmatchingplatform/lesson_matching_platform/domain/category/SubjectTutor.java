package com.lessonmatchingplatform.lesson_matching_platform.domain.category;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "subject_tutor_tutor_id_subject_id",
                columnNames = {"tutor_id", "subject_id"}
        )
})
@Entity
public class SubjectTutor extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private TutorAccount tutorAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    protected SubjectTutor() {}

    private SubjectTutor(TutorAccount tutorAccount, Subject subject) {
        this.tutorAccount = tutorAccount;
        this.subject = subject;
    }

    public static SubjectTutor of(TutorAccount tutorAccount, Subject subject) {
        return new SubjectTutor(tutorAccount, subject);
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
