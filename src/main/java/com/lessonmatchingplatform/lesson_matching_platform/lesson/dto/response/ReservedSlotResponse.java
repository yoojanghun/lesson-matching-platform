package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservedSlotResponse(
        Long reservationId,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate lessonDate,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        ReservationStatus reservationStatus
) {

    public static ReservedSlotResponse from(Reservation entity) {
        return new ReservedSlotResponse(
                entity.getReservationId(),
                entity.getLessonDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getReservationStatus()
        );
    }
}
