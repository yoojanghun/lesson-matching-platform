package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;

import java.util.List;
import java.util.Optional;

public interface MatchingRepositoryCustom {
    List<MyMatchingResponseAsTutor> findAllByTutorId(Long tutorId);
    List<Matching> findAllByStudentId(Long studentId);
    Boolean existsActiveMatching(Long studentId, Long tutorId);
    Boolean hasAlreadyReviewedTutor(Long tutorId, Long studentId);
}
