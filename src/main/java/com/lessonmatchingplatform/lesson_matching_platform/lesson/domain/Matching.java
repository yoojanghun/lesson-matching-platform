package com.lessonmatchingplatform.lesson_matching_platform.lesson.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
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

    @Column(nullable = false)
    private Integer pricePerLesson;

    protected Matching() {}

    private Matching(StudentAccount studentAccount, TutorAccount tutorAccount, String requestMsg, MatchingStatus status) {
        this.studentAccount = studentAccount;
        this.tutorAccount = tutorAccount;
        this.requestMsg = requestMsg;
        this.status = status;
        this.pricePerLesson = null;
    }

    public static Matching of(StudentAccount studentAccount, TutorAccount tutorAccount, String requestMsg, MatchingStatus status) {
        return new Matching(studentAccount, tutorAccount, requestMsg, status);
    }

    public void cancelMatching() {
        if (this.status != MatchingStatus.PENDING) {
            throw new IllegalStateException("대기 중(PENDING)인 매칭 요청만 취소할 수 있습니다.");
        }
        this.status = MatchingStatus.CANCELLED;
    }

    public void setPrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("레슨비는 0원 이상이어야 합니다.");
        }
        this.pricePerLesson = price;
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
