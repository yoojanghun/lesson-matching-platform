package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class ScheduleException extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exceptionId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @Column(nullable = false)
    private LocalDate exceptionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ExceptionType exceptionType;

    protected ScheduleException() {}

    private ScheduleException(LocalDate exceptionDate, LocalTime startTime, LocalTime endTime, ExceptionType exceptionType) {
        this.exceptionDate = exceptionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exceptionType = exceptionType;
    }

    public static ScheduleException of(LocalDate exceptionDate, LocalTime startTime, LocalTime endTime, ExceptionType exceptionType) {
        return new ScheduleException(exceptionDate, startTime, endTime, exceptionType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleException that)) return false;
        return this.exceptionId != null && Objects.equals(this.exceptionId, that.exceptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(exceptionId);
    }
}
