package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReservationResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.ReservationRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    Optional<Reservation> findByReservationIdAndMatching_StudentAccount_StudentId(Long matchingId, Long studentId);

    Optional<Reservation> findByReservationIdAndTutorAccount_TutorId(Long reservationId, Long tutorId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.matching m JOIN FETCH m.studentAccount sa JOIN FETCH sa.userAccount ua WHERE m.matchingId = :matchingId ORDER BY r.lessonDate DESC, r.startTime DESC")
    Slice<Reservation> findReservationsWithDetailsByMatchingId(Long matchingId, Pageable pageable);
}
