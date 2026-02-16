package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponseAsTutor;

import java.util.List;
import java.util.Optional;

public interface MatchingRepositoryCustom {
    Optional<Matching> findByIdWithDetails(Long matchingId);
    List<MyMatchingResponseAsTutor> findAllByTutorId(Long tutorId);
    List<Matching> findAllByStudentId(Long studentId);
    Boolean existsActiveMatching(Long studentId, Long tutorId);
    Boolean hasAlreadyReviewedTutor(Long tutorId, Long studentId);
}
