package com.lessonmatchingplatform.lesson_matching_platform.domain.lesson;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.StudentAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class Matching extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchingId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentAccount studentAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private LessonPost lessonPost;

    @Column(length = 500, nullable = false)
    private String requestMsg;

    @Column(length = 20, nullable = false)
    private String status;

    protected Matching() {}

    private Matching(StudentAccount studentAccount, LessonPost lessonPost, String requestMsg, String status) {
        this.studentAccount = studentAccount;
        this.lessonPost = lessonPost;
        this.requestMsg = requestMsg;
        this.status = status;
    }

    public static Matching of(StudentAccount studentAccount, LessonPost lessonPost, String requestMsg, String status) {
        return new Matching(studentAccount, lessonPost, requestMsg, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matching that)) return false;
        return this.matchingId != null && Objects.equals(this.matchingId, that.matchingId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(matchingId);
    }
}
