package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GoalTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoalTutorRepository extends JpaRepository<GoalTutor, Long> {

    @Query("SELECT gt FROM GoalTutor gt JOIN FETCH gt.LessonGoal lg WHERE gt.tutorAccount.tutorId IN :tutorsId")
    List<GoalTutor> findAllByTutorIdIn(List<Long> tutorsId);
}
