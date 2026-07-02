package com.lessonmatchingplatform.lesson_matching_platform.account.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GenderType;

import java.time.LocalDate;

public record StudentMyResponse(
        String userId,
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String email,
        String introduction
) {

    public static StudentMyResponse from(StudentAccount entity) {
        return new StudentMyResponse(
                entity.getUserAccount().getUserId(),
                entity.getUserAccount().getName(),
                entity.getUserAccount().getGender(),
                entity.getUserAccount().getBirthDate(),
                entity.getUserAccount().getPhoneNumber(),
                entity.getUserAccount().getEmail(),
                entity.getIntroduction()
        );
    }
}
