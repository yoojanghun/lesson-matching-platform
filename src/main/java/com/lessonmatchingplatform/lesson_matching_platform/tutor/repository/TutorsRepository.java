package com.lessonmatchingplatform.lesson_matching_platform.tutor.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.querydsl.TutorsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TutorsRepository extends JpaRepository<TutorAccount, Long>, TutorsRepositoryCustom {

    @Query("SELECT t FROM TutorAccount t JOIN FETCH t.userAccount u WHERE t.tutorId = :tutorId")
    Optional<TutorAccount> findProfileById(@Param("tutorId") Long tutorId);

    @Query("SELECT DISTINCT t FROM TutorAccount t " +
           "JOIN FETCH t.userAccount u " +
           "WHERE t.profileStatus = :profileStatus")
    List<TutorAccount> findAllByProfileStatus(@Param("profileStatus") ProfileStatus profileStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ta FROM TutorAccount ta WHERE ta.tutorId = :tutorId")
    Optional<TutorAccount> findByIdWithLock(@Param("tutorId") Long tutorId);

    /**
     * [벤치마크 Case B: 조건부/원자적 UPDATE]
     * 비관적 락 없이 단 1회의 원자적 UPDATE 쿼리로 reviewCount와 averageRating을 갱신.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE TutorAccount ta SET " +
           "ta.reviewCount = ta.reviewCount + 1, " +
           "ta.averageRating = CASE WHEN ta.reviewCount = 0 THEN :rating " +
           "  ELSE ROUND((ta.averageRating * ta.reviewCount + :rating) / (ta.reviewCount + 1), 1) END " +
           "WHERE ta.tutorId = :tutorId")
    int updateRatingAtomically(@Param("tutorId") Long tutorId, @Param("rating") BigDecimal rating);
}
