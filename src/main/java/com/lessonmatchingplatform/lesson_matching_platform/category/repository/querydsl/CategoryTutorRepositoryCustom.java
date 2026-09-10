package com.lessonmatchingplatform.lesson_matching_platform.category.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;

import java.util.List;

public interface CategoryTutorRepositoryCustom {
    TutorProfileResponse findProfileResponseById(Long tutorId);

    List<TutorProfileResponse> findProfileResponseByIds(List<Long> tutorIds);
}
