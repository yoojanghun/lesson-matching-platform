package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.LessonReview;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.ReviewRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<LessonReview, Long>, ReviewRepositoryCustom {
}
