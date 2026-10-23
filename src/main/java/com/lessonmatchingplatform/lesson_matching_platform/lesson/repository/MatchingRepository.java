package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.MatchingRepositoryCustom;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long>, MatchingRepositoryCustom {
    Optional<Matching> findByStudentAccount_StudentIdAndStatus(Long studentId, MatchingStatus status);

    Optional<Matching> findByStudentAccount_StudentIdAndTutorAccount_TutorIdAndStatus(Long studentId, Long tutorId, MatchingStatus status);

    Optional<Matching> findByMatchingIdAndStudentAccount_StudentId(Long matchingId, Long studentId);

    Optional<Matching> findByMatchingIdAndTutorAccount_TutorId(Long matchingId, Long tutorId);

    @Query("SELECT m FROM Matching m JOIN FETCH m.studentAccount sa JOIN FETCH m.tutorAccount ta JOIN FETCH sa.userAccount sua JOIN FETCH ta.userAccount tua WHERE m.matchingId = :matchingId")
    Matching findWithDetailsByMatchingId(Long matchingId);

    /**
     * 튜터 매칭 승인 처리 + SMS 발송에 필요한 studentAccount.userAccount 즉시 로드용 쿼리
     * - studentAccount → userAccount.phoneNumber 접근 시 Lazy 로딩 문제 방지
     */
    @Query("SELECT m FROM Matching m " +
            "JOIN FETCH m.studentAccount sa " +
            "JOIN FETCH sa.userAccount ua " +
            "WHERE m.matchingId = :matchingId AND m.tutorAccount.tutorId = :tutorId")
    Optional<Matching> findByMatchingIdAndTutorIdWithStudent(
            @Param("matchingId") Long matchingId,
            @Param("tutorId") Long tutorId);

    /**
     * 상태 조건부 원자적 UPDATE (동시성 보호)
     * PENDING 상태일 때만 newStatus로 업데이트
     * @return 업데이트된 행 수 (0이면 이미 다른 스레드가 처리한 것)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Matching m SET m.status = :newStatus WHERE m.matchingId = :matchingId AND m.tutorAccount.tutorId = :tutorId AND m.status = 'PENDING'")
    int updateStatusIfPending(
            @Param("matchingId") Long matchingId,
            @Param("tutorId") Long tutorId,
            @Param("newStatus") MatchingStatus newStatus);
}
