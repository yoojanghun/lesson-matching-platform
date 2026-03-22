package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;

import java.util.List;
import java.util.Map;

public record CategoriesAndTutorsResponse(
        List<CategoryResponse> categoryNames,
        Map<Long, List<TutorsResponse>> popularTutors
) {
    public static CategoriesAndTutorsResponse of(List<Category> categories, Map<Long, List<TutorAccount>> tutors) {
        List<CategoryResponse> categoryNames = categories.stream()
                .map(CategoryResponse::from)
                .toList();

        Map<Long, List<TutorsResponse>> tutorResponsesMap = tutors.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, // 카테고리 ID는 그대로 유지
                        entry -> entry.getValue().stream() // 리스트 내부를 변환
                                .map(TutorsResponse::from)
                                .toList()
                ));

        return new CategoriesAndTutorsResponse(
                categoryNames,
                tutorResponsesMap
        );
    }
}
