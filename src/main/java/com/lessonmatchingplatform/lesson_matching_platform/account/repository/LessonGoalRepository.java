package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.LessonGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonGoalRepository extends JpaRepository<LessonGoal, Long> {
}
