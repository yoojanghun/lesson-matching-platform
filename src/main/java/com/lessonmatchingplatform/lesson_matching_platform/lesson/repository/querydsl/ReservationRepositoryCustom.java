package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepositoryCustom {
    boolean existsOverlappingReservation(Long tutorId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
