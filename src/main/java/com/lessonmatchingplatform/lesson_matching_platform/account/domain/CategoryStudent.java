package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
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
                        name = "uk_category_student",
                        columnNames = {"student_id", "category_id"}
                )
        }
)
@Entity
public class CategoryStudent extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentAccount studentAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private CategoryStudent(StudentAccount studentAccount, Category category) {
        this.studentAccount = studentAccount;
        this.category = category;
    }

    protected CategoryStudent() {}

    public static CategoryStudent of(StudentAccount studentAccount, Category category) {
        return new CategoryStudent(studentAccount, category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryStudent that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
