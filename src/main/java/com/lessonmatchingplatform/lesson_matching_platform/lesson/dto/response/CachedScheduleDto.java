package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CachedScheduleDto(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
    public static CachedScheduleDto from(Schedule schedule) {
        return new CachedScheduleDto(schedule.getDayOfWeek(), schedule.getStartTime(), schedule.getEndTime());
    }
}
