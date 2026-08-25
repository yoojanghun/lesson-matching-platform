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
                        name = "uk_style_tutor",
                        columnNames = {"tutor_id", "style_id"}
                )
        }
)
@Entity
public class StyleTutor extends AuditingFields {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "style_id", nullable = false)
    private TutorStyle tutorStyle;

    private StyleTutor(TutorAccount tutorAccount, TutorStyle tutorStyle) {
        this.tutorAccount = tutorAccount;
        this.tutorStyle = tutorStyle;
    }

    protected StyleTutor() {}

    public static StyleTutor of(TutorAccount tutorAccount, TutorStyle tutorStyle) {
        return new StyleTutor(tutorAccount, tutorStyle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StyleTutor that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
