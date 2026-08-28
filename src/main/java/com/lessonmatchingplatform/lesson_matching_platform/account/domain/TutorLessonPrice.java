package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(exclude = "tutorAccount")
@Getter
@Entity
public class TutorLessonPrice extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(nullable = false)
    private Integer price;

    private TutorLessonPrice(TutorAccount tutorAccount, String className, Integer price) {
        this.tutorAccount = tutorAccount;
        this.className = className;
        this.price = price;
    }

    protected TutorLessonPrice() {}

    public static TutorLessonPrice of(TutorAccount tutorAccount, String className, Integer price) {
        return new TutorLessonPrice(tutorAccount, className, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TutorLessonPrice that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
