package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorLessonPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorLessonPriceRepository extends JpaRepository<Long, TutorLessonPrice> {
    List<TutorLessonPrice> findAllByTutorAccount_TutorIdIn(List<Long> tutorIds);
}
