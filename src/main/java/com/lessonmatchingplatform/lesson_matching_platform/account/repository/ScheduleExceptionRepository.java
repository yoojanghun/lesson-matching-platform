package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ScheduleException;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl.ScheduleExceptionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long>, ScheduleExceptionRepositoryCustom {
    List<ScheduleException> findAllByTutorAccount_TutorIdAndExceptionDate(Long tutorId, LocalDate date);
}
