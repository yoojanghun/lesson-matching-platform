package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl.MatchingRepositoryCustom;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long>, MatchingRepositoryCustom {
    Optional<Matching> findByStudentAccount_StudentIdAndStatus(Long studentId, MatchingStatus status);

    Optional<Matching> findByMatchingIdAndStudentAccount_StudentId(Long matchingId, Long studentId);
}
