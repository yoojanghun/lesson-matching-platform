package com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ScheduleException;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleExceptionRepositoryCustom {
    List<ScheduleException> findByTutorIdAndDateRange(Long tutorId, LocalDate startTime, LocalDate endTime);
}
