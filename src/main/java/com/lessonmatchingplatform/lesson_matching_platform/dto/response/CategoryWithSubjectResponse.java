package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;

import java.util.List;

public record CategoryWithSubjectResponse(
        Long categoryId,
        String name,
        List<SubjectResponse> subjects      // Response로 순환참조 방지
) {

    public static CategoryWithSubjectResponse from(Category entity) {

        List<SubjectResponse> subjects = entity.getSubjects().stream()
                .map(SubjectResponse::from)
                .toList();

        return new CategoryWithSubjectResponse(
                entity.getCategoryId(),
                entity.getName(),
                subjects
        );
    }
}
