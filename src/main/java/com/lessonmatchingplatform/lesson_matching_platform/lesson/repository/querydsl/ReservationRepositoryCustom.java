package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepositoryCustom {
    boolean existsOverlappingReservation(Long tutorId, LocalDate date, LocalTime startTime, LocalTime endTime);

    List<Reservation> findActiveReservationsByTutorIdAndDateRange(Long tutorId, LocalDate startDate, LocalDate endDate);
}
