package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;

import java.util.Optional;

public interface MatchingRepositoryCustom {
    Optional<Matching> findByIdWithDetails(Long matchingId);
}
