package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record MyMatchingResponseAsStudent(
        Long MatchingId,
        String tutorName,
        Set<String> category,
        Set<String> subject,
        String requestMsg,
        MatchingStatus status,
        LocalDateTime createdAt
) {
    public static MyMatchingResponseAsStudent from(Matching entity) {
        TutorAccount tutorAccount = entity.getTutorAccount();
        UserAccount userAccount = tutorAccount.getUserAccount();

        Set<String> categories = tutorAccount.getCategoryTutorSet().stream()
                .map(categoryTutor ->
                        categoryTutor.getCategory().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> subjects = tutorAccount.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        return new MyMatchingResponseAsStudent(
                entity.getMatchingId(),
                userAccount.getName(),
                categories,
                subjects,
                entity.getRequestMsg(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
