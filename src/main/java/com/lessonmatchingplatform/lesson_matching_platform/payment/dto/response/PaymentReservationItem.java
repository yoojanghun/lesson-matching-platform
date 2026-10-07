package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record PaymentReservationItem(
        Long reservationId,
        LocalDate lessonDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer appliedPrice
) {
    public static PaymentReservationItem from(Reservation reservation) {
        return new PaymentReservationItem(
                reservation.getReservationId(),
                reservation.getLessonDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getAppliedPrice()
        );
    }
}
