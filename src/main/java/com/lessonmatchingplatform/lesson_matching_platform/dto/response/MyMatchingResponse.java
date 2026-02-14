package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;

import java.time.LocalDate;

public record MyMatchingResponse(
        String requestMsg,
        MatchingStatus status,
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String email
) {

    public static MyMatchingResponse from(Matching entity) {

        UserAccount userAccount = entity.getStudentAccount().getUserAccount();

        return new MyMatchingResponse(
                entity.getRequestMsg(),
                entity.getStatus(),
                userAccount.getName(),
                userAccount.getGender(),
                userAccount.getBirthDate(),
                userAccount.getPhoneNumber(),
                userAccount.getEmail()
        );
    }
}
