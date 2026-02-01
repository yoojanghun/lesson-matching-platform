package com.lessonmatchingplatform.lesson_matching_platform.domain.category;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
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
public class Subject extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @ToString.Exclude
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private final Set<Genre> genres = new LinkedHashSet<>();

    protected Subject() {}

    private Subject(Category category, String name) {
        this.category = category;
        this.name = name;
    }

    public static Subject of(Category category, String name) {
        return new Subject(category, name);
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
