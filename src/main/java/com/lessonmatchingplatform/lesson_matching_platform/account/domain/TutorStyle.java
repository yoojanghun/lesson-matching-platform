package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.StyleType;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 150)
    private StyleType styleType;

    private TutorStyle (StudentAccount studentAccount, StyleType styleType) {
        this.studentAccount = studentAccount;
        this.styleType = styleType;
    }

    protected TutorStyle() {}

    public static TutorStyle of (StudentAccount studentAccount, StyleType styleType) {
        return new TutorStyle(studentAccount, styleType);
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
