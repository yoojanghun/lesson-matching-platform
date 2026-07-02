package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public record TutorsResponse(
        Long tutorId,
        String name,
        String title,
        BigDecimal averageRating,
        Integer reviewCount,
        Set<CategoryType> categories,
        Set<String> subjects
){
    public static TutorsResponse from(TutorAccount entity) {

        // batch_fetch_size=100 설정으로 tutors들의 categories들을 한두번의 쿼리로 빠르게 채워줌
        Set<CategoryType> categories = entity.getCategoryTutorSet().stream()
                .map(categoryTutor ->
                        categoryTutor.getCategory().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        return new TutorsResponse(
                entity.getTutorId(),
                entity.getUserAccount().getName(),              // fetchJoin 사용으로 추가 쿼리 안 나감.
                entity.getTitle(),
                entity.getAverageRating(),
                entity.getReviewCount(),
                categories,
                subjects
        );
    }
}
