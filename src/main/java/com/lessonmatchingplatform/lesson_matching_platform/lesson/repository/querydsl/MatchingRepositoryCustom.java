package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MatchingRepositoryCustom {

    Page<MyMatchingResponseAsTutor> findMatchingsByTutorId(Long tutorId, Pageable pageable);

    Page<Matching> findMatchingsByStudentId(Long studentId, Pageable pageable);

    Boolean existsActiveMatching(Long studentId, Long tutorId);

    Boolean hasAlreadyReviewedTutor(Long tutorId, Long studentId);
}
