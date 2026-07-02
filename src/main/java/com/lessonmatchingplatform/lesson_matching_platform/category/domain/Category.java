package com.lessonmatchingplatform.lesson_matching_platform.category.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "name")
})
@Entity
public class Category extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false, unique = true)
    private CategoryType name;

    @ToString.Exclude
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private final Set<Subject> subjects = new LinkedHashSet<>();

    protected Category() {}

    private Category(CategoryType name) {
        this.name = name;
    }

    public static Category of(CategoryType name) {
        return new Category(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return this.categoryId != null && Objects.equals(this.categoryId, category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(categoryId);
    }
}
