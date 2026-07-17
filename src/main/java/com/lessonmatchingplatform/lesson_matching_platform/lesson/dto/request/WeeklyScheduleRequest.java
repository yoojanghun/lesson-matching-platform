package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WeeklyScheduleRequest(
        @NotNull(message = "요일은 필수입니다.")
        DayOfWeek dayOfWeek,

        @NotNull(message = "시작 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {
    public WeeklyScheduleRequest {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }
    }
}
