package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TutorsRepositoryCustom {
    Page<TutorAccount> searchTutors(TutorSearchCondition condition, Pageable pageable);
}
