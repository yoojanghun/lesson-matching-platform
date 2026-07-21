package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    void deleteByTutorAccount_TutorId(Long tutorId);

    List<Schedule> findAllByTutorAccount_TutorIdAndDayOfWeek(Long tutorId, DayOfWeek dayOfWeek);

    List<Schedule> findAllByTutorAccount_TutorId(Long tutorId);
}
