package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TutorResponse(
    Long tutorId,
    String name,
    GenderType gender,
    String email,
    String introduction,
    List<String> experiences,
    String title,
    List<String> educations,
    Set<String> locations,
    Set<CategoryType> categories,
    Set<SubjectType> subjects
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

        Set<SubjectType> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        return new TutorResponse(
                entity.getTutorId(),
                entity.getUserAccount().getName(),
                entity.getUserAccount().getGender(),
                entity.getUserAccount().getEmail(),
                entity.getIntroduction(),
                entity.getExperiences(),
                entity.getTitle(),
                entity.getEducations(),
                locations,
                categories,
                subjects
        );
    }
}
