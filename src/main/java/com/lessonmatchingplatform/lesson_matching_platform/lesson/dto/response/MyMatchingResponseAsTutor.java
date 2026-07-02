package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyMatchingResponseAsTutor(
        Long matchingId,
        String requestMsg,
        MatchingStatus status,
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String email,
        LocalDateTime createdAt
) {

    public static MyMatchingResponseAsTutor from(Matching entity) {

        UserAccount userAccount = entity.getStudentAccount().getUserAccount();

        return new MyMatchingResponseAsTutor(
                entity.getMatchingId(),
                entity.getRequestMsg(),
                entity.getStatus(),
                userAccount.getName(),
                userAccount.getGender(),
                userAccount.getBirthDate(),
                userAccount.getPhoneNumber(),
                userAccount.getEmail(),
                entity.getCreatedAt()
        );
    }
}
