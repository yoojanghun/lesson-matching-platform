package com.lessonmatchingplatform.lesson_matching_platform.repository;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl.MatchingRepositoryCustom;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long>, MatchingRepositoryCustom {
    Optional<Matching> findByStudentAccount_StudentIdAndStatus(Long studentId, MatchingStatus status);
}
