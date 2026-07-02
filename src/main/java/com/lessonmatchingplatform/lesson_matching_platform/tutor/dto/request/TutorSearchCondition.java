package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectType;

// TODO: 나중에 검색 조건 추가되면 수정
public record TutorSearchCondition(
        CategoryType category,
        SubjectType subject
) {

}
