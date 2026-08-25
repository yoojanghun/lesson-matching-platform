package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class InterestCategory extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestId;

    @Column(nullable = false, length = 150)
    private CategoryType name;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentAccount studentAccount;

    private InterestCategory(StudentAccount studentAccount, CategoryType name) {
        this.studentAccount = studentAccount;
        this.name = name;
    }

    protected InterestCategory() {}

    public static InterestCategory of(StudentAccount studentAccount, CategoryType name) {
        return new InterestCategory(studentAccount, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterestCategory that)) return false;
        return this.interestId != null && Objects.equals(interestId, that.interestId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(interestId);
    }
}
