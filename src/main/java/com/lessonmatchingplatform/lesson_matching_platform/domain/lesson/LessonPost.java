package com.lessonmatchingplatform.lesson_matching_platform.domain.lesson;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class LessonPost extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 50)
    private String location;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    protected LessonPost() {}

    public LessonPost(TutorAccount tutorAccount, String title, String location, String content) {
        this.tutorAccount = tutorAccount;
        this.title = title;
        this.location = location;
        this.content = content;
    }

    private static LessonPost of(TutorAccount tutorAccount, String title, String location, String content) {
        return new LessonPost(tutorAccount, title, location, content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonPost that)) return false;
        return this.postId != null && Objects.equals(this.postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(postId);
    }
}
