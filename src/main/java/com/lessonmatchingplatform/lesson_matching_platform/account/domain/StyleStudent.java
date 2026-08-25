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
                        name = "uk_style_student",
                        columnNames = {"student_id", "style_id"}
                )
        }
)
@Entity
public class StyleStudent extends AuditingFields {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentAccount studentAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id", nullable = false)
    private TutorStyle tutorStyle;

    private StyleStudent(StudentAccount studentAccount, TutorStyle tutorStyle) {
        this.studentAccount = studentAccount;
        this.tutorStyle = tutorStyle;
    }

    protected StyleStudent() {}

    public static StyleStudent of(StudentAccount studentAccount, TutorStyle tutorStyle) {
        return new StyleStudent(studentAccount, tutorStyle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StyleStudent that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
