package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.ReservationRepositoryCustom;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.ReservationStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    Optional<Reservation> findByReservationIdAndMatching_StudentAccount_StudentId(Long matchingId, Long studentId);

    Optional<Reservation> findByReservationIdAndTutorAccount_TutorId(Long reservationId, Long tutorId);

    @Query("SELECT r FROM Reservation r WHERE r.reservationId IN :reservationIds AND r.matching.id = :matchingId AND r.reservationStatus = :status AND r.payment IS NULL")
    List<Reservation> findValidReservationsForPayment(@Param("reservationIds") List<Long> reservationIds, @Param("matchingId") Long matchingId, @Param("status")ReservationStatus status);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.matching m JOIN FETCH m.studentAccount sa JOIN FETCH sa.userAccount ua WHERE m.matchingId = :matchingId ORDER BY r.lessonDate DESC, r.startTime DESC")
    Slice<Reservation> findReservationsWithDetailsByMatchingId(Long matchingId, Pageable pageable);
}
