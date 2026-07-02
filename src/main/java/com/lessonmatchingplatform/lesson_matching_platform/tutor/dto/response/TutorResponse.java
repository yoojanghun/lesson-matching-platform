package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GenderType;

import java.util.Set;
import java.util.stream.Collectors;

public record TutorResponse(
    Long tutorId,
    String name,
    GenderType gender,
    String email,
    String introduction,
    String career,
    String title,
    String content,
    Set<String> locations,
    Set<CategoryType> categories,
    Set<String> subjects
) {

    public static TutorResponse from(TutorAccount entity) {

        Set<String> locations = entity.getLocationTutorSet().stream()
                .map(locationTutor ->
                        locationTutor.getLocation().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<CategoryType> categories = entity.getCategoryTutorSet().stream()
                .map(categoryTutor ->
                        categoryTutor.getCategory().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        return new TutorResponse(
                entity.getTutorId(),
                entity.getUserAccount().getName(),
                entity.getUserAccount().getGender(),
                entity.getUserAccount().getEmail(),
                entity.getIntroduction(),
                entity.getCareer(),
                entity.getTitle(),
                entity.getContent(),
                locations,
                categories,
                subjects
        );
    }
}
