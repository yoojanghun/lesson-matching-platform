package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ScheduleException;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ExceptionType;

import java.time.LocalDate;
import java.time.LocalTime;

public record CachedScheduleExceptionDto(
        LocalDate exceptionDate,
        LocalTime startTime,
        LocalTime endTime,
        ExceptionType exceptionType
) {
    public static CachedScheduleExceptionDto from(ScheduleException exception) {
        return new CachedScheduleExceptionDto(
                exception.getExceptionDate(),
                exception.getStartTime(),
                exception.getEndTime(),
                exception.getExceptionType()
        );
    }
}
