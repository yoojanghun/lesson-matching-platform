package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;

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
