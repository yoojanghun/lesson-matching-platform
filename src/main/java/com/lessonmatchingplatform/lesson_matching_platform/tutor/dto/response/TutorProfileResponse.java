package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;

import java.util.List;

public record TutorProfileResponse(
        Long tutorId,
        String name,
        GenderType gender,
        String email,
        String phoneNumber,
        String title,
        String content,
        String introduction,
        String career,
        List<String> locations,
        List<CategoryType> categories,
        List<SubjectType> subjects
) {

    public static TutorProfileResponse from(TutorAccount entity) {
        List<String> locations = entity.getLocationTutorSet().stream()
                .map(locationTutor -> locationTutor.getLocation().getName())
                .toList();

        List<CategoryType> categories = entity.getCategoryTutorSet().stream()
                .map(categoryTutor -> categoryTutor.getCategory().getName())
                .toList();

        List<SubjectType> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor -> subjectTutor.getSubject().getName())
                .toList();

        return new TutorProfileResponse(
                entity.getTutorId(),
                entity.getUserAccount().getName(),
                entity.getUserAccount().getGender(),
                entity.getUserAccount().getEmail(),
                entity.getUserAccount().getPhoneNumber(),
                entity.getTitle(),
                entity.getContent(),
                entity.getIntroduction(),
                entity.getCareer(),
                locations,
                categories,
                subjects
        );
    }
}
