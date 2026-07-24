package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ExceptionType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleExceptionRequest(
        @NotNull(message = "요일은 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate exceptionDate,

        @NotNull(message = "시작 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        @NotNull(message = "예외 유형은 필수입니다")
        ExceptionType exceptionType
) {
    public ScheduleExceptionRequest {
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }
    }
}
