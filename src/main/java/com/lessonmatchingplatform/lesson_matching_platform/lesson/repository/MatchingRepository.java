package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.MatchingRepositoryCustom;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long>, MatchingRepositoryCustom {
    Optional<Matching> findByStudentAccount_StudentIdAndStatus(Long studentId, MatchingStatus status);

    Optional<Matching> findByMatchingIdAndStudentAccount_StudentId(Long matchingId, Long studentId);

    Optional<Matching> findByMatchingIdAndTutorAccount_TutorId(Long matchingId, Long tutorId);

    @Query("SELECT m FROM Matching m JOIN FETCH m.studentAccount sa JOIN FETCH m.tutorAccount ta JOIN FETCH sa.userAccount sua JOIN FETCH ta.userAccount tua WHERE m.matchingId = :matchingId")
    Matching findWithDetailsByMatchingId(Long matchingId);
}
