package com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TutorsRepositoryCustom {
    Page<TutorAccount> searchTutors(TutorSearchCondition condition, Pageable pageable);
    List<TutorAccount> searchPopularTutors(Long categoryId);
    Optional<TutorAccount> searchTutor(Long tutorId);
}
