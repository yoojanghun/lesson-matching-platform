package com.lessonmatchingplatform.lesson_matching_platform.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@EntityListeners(AuditingFields.class)
@Entity
public class Category extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    protected Category() {}

    private Category(String name) {
        this.name = name;
    }

    public static Category of(String name) {
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
