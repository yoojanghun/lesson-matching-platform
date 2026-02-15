package com.lessonmatchingplatform.lesson_matching_platform.domain.lesson;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "student_id"),
        @Index(columnList = "tutor_id")
})
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
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @Column(length = 500, nullable = false)
    private String requestMsg;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MatchingStatus status;

    @OneToOne(mappedBy = "matching", cascade = CascadeType.ALL)
    private LessonReview lessonReview;

    protected Matching() {}

    private Matching(StudentAccount studentAccount, TutorAccount tutorAccount, String requestMsg, MatchingStatus status) {
        this.studentAccount = studentAccount;
        this.tutorAccount = tutorAccount;
        this.requestMsg = requestMsg;
        this.status = status;
    }

    public static Matching of(StudentAccount studentAccount, TutorAccount tutorAccount, String requestMsg, MatchingStatus status) {
        return new Matching(studentAccount, tutorAccount, requestMsg, status);
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
