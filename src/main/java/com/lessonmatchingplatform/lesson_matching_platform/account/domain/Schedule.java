package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class Schedule extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    protected Schedule() {}

    private Schedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Schedule of(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return new Schedule(dayOfWeek, startTime, endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule that)) return false;
        return this.scheduleId != null && Objects.equals(this.scheduleId, that.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheduleId);
    }
}
