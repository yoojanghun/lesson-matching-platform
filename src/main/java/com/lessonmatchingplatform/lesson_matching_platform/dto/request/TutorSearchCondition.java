package com.lessonmatchingplatform.lesson_matching_platform.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.type.SubjectType;

// TODO: 나중에 검색 조건 추가되면 수정
public record TutorSearchCondition(
        CategoryType category,
        SubjectType subject
) {

}
