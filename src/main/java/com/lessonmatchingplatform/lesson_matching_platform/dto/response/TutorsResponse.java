package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import java.util.Set;
import java.util.stream.Collectors;

public record TutorsResponse(
        String name,
        String title,
        Set<String> categories,
        Set<String> subjects
){
    public static TutorsResponse from(TutorAccount entity) {

        Set<String> categories = entity.getCategoryTutorSet().stream()
                .map(categoryTutor ->
                        categoryTutor.getCategory().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        return new TutorsResponse(
                entity.getUserAccount().getName(),
                entity.getTitle(),
                categories,
                subjects
        );
    }
}
