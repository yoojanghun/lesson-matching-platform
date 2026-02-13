package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import java.util.Set;
import java.util.stream.Collectors;

public record TutorsResponse(
        Long tutorId,
        String name,
        String title,
        Set<String> categories,
        Set<String> subjects
){
    public static TutorsResponse from(TutorAccount entity) {

        // batch_fetch_size=100 설정으로 tutors들의 categories들을 한두번의 쿼리로 빠르게 채워줌
        Set<String> categories = entity.getCategoryTutorSet().stream()
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
                categories,
                subjects
        );
    }
}
