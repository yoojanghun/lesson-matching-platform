package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class TutorStyle extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long styleId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentAccount studentAccount;

    @Column(nullable = false, length = 150)
    private String style;

    private TutorStyle (StudentAccount studentAccount, String style) {
        this.studentAccount = studentAccount;
        this.style = style;
    }

    protected TutorStyle() {}

    public static TutorStyle of (StudentAccount studentAccount, String style) {
        return new TutorStyle(studentAccount, style);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TutorStyle that)) return false;
        return this.styleId != null && Objects.equals(styleId, that.styleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(styleId);
    }
}
