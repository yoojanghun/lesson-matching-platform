package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.ReservationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
    Optional<Reservation> findByReservationIdAndMatching_StudentAccount_StudentId(Long matchingId, Long studentId);
}
