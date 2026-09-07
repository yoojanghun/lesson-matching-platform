package com.lessonmatchingplatform.lesson_matching_platform.tutor.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.querydsl.TutorsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
