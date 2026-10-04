package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.LessonReview;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.ReviewRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<LessonReview, Long>, ReviewRepositoryCustom {

    @Query("SELECT lr FROM LessonReview lr JOIN FETCH lr.matching m JOIN FETCH m.tutorAccount ta JOIN FETCH m.studentAccount WHERE lr.commentId = :reviewId")
    Optional<LessonReview> findByIdWithDetails(@Param("reviewId") Long reviewId);
}
