package com.lessonmatchingplatform.lesson_matching_platform.category.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;

public interface CategoryTutorRepositoryCustom {
    TutorProfileResponse findProfileResponseById(Long tutorId);
}
