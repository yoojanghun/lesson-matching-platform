package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ExceptionType;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ScheduleException;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleExceptionResponse(
        Long exceptionId,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate exceptionDate,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        ExceptionType exceptionType
) {

    public static ScheduleExceptionResponse from(ScheduleException entity) {
        return new ScheduleExceptionResponse(
                entity.getExceptionId(),
                entity.getExceptionDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getExceptionType()
        );
    }
}
