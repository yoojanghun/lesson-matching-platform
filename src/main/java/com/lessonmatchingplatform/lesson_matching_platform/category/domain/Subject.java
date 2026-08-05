package com.lessonmatchingplatform.lesson_matching_platform.category.domain;

import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;
import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class Subject extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false, unique = true)
    private SubjectType name;

    @Column(nullable = false)
    private Integer displayOrder;

    protected Subject() {}

    private Subject(Category category, SubjectType name, Integer displayOrder) {
        this.category = category;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public static Subject of(Category category, SubjectType name, Integer displayOrder) {
        return new Subject(category, name, displayOrder);
    }

    public void update(Category category, SubjectType name, Integer displayOrder) {
        if (category != null) {
            this.category = category;
        }
        if (name != null) {
            this.name = name;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject subject)) return false;
        return this.subjectId != null && Objects.equals(this.subjectId, subject.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(subjectId);
    }
}
