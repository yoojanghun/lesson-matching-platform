package com.lessonmatchingplatform.lesson_matching_platform.domain.lesson;

import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Subject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString
@Getter
@Entity
public class PostSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private LessonPost lessonPost;

    protected PostSubject() {}

    private PostSubject(Subject subject, LessonPost lessonPost) {
        this.subject = subject;
        this.lessonPost = lessonPost;
    }

    public static PostSubject of(Subject subject, LessonPost lessonPost) {
        return new PostSubject(subject, lessonPost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostSubject that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
