package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import java.util.List;

public record TutorScheduleResponse(
        Long tutorId,
        List<WeeklyScheduleResponse> weeklySchedules,
        List<ScheduleExceptionResponse> scheduleExceptions,
        List<ReservedSlotResponse> reservedSlots
) {

    public static TutorScheduleResponse of(Long tutorId, List<WeeklyScheduleResponse> weeklySchedules, List<ScheduleExceptionResponse> scheduleExceptions, List<ReservedSlotResponse> reservedSlots) {
        return new TutorScheduleResponse(
                tutorId,
                weeklySchedules,
                scheduleExceptions,
                reservedSlots
        );
    }
}