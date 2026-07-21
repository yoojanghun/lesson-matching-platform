package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepositoryCustom {
    boolean existsOverlappingReservation(Long tutorId, LocalDate date, LocalTime startTime, LocalTime endTime);

    List<Reservation> findActiveReservationsByTutorIdAndDateRange(Long tutorId, LocalDate startDate, LocalDate endDate);

    Page<ReservationResponse> findTutorReservations(Long tutorId, ReservationStatus status, Pageable pageable);
}
