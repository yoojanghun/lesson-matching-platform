package com.lessonmatchingplatform.lesson_matching_platform.domain.lesson;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_lesson_review_matching_id",
                columnNames = {"matching_id"}
        )
})
@Entity
public class LessonReview extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false, unique = true)
    private Matching matching;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(precision = 2, scale = 1, nullable = false)         // 전체 2자리수, 소숫점 이하 1자리수
    private BigDecimal rating;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isAnonymous = true;

    protected LessonReview() {}

    private LessonReview(Matching matching, String content, BigDecimal rating, Boolean isAnonymous) {
        this.matching = matching;
        this.content = content;
        this.rating = rating;
        this.isAnonymous = isAnonymous;
    }

    public static LessonReview of(Matching matching, String content, BigDecimal rating, Boolean isAnonymous) {
        return new LessonReview(matching, content, rating, isAnonymous);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonReview that)) return false;
        return this.commentId != null && Objects.equals(this.commentId, that.commentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commentId);
    }
}
