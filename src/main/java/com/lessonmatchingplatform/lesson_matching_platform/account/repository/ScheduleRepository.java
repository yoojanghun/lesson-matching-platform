package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Schedule;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    void deleteByTutorAccount_TutorId(Long tutorId);

    List<Schedule> findAllByTutorAccount_TutorIdAndDayOfWeek(Long tutorId, DayOfWeek dayOfWeek);
}
