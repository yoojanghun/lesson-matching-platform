package com.lessonmatchingplatform.lesson_matching_platform.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "name")
})
@Entity
public class Genre extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    protected Genre() {}

    private Genre(String name, Subject subject) {
        this.name = name;
        this.subject = subject;
    }

    public static Genre of(String name, Subject subject) {
        return new Genre(name, subject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre genre)) return false;
        return this.genreId != null && Objects.equals(this.genreId, genre.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(genreId);
    }
}
