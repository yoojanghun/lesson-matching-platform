package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;

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
    Set<String> categories,
    Set<String> subjects
) {

    public static TutorResponse from(TutorAccount entity) {

        Set<String> locations = entity.getLocationTutorSet().stream()
                .map(locationTutor ->
                        locationTutor.getLocation().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> categories = entity.getCategoryTutorSet().stream()
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
