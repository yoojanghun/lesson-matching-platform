package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WeeklyScheduleResponse(
        Long scheduleId,

        DayOfWeek dayOfWeek,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {

    public static WeeklyScheduleResponse from(Schedule entity) {
        return new WeeklyScheduleResponse(
                entity.getScheduleId(),
                entity.getDayOfWeek(),
                entity.getStartTime(),
                entity.getEndTime()
        );
    }
}
