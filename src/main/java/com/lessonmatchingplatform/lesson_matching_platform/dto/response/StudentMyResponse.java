package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.type.RoleType;

import java.time.LocalDate;

public record StudentMyResponse(
        String userId,
        String name,
        RoleType role,
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
                entity.getUserAccount().getRole(),
                entity.getUserAccount().getGender(),
                entity.getUserAccount().getBirthDate(),
                entity.getUserAccount().getPhoneNumber(),
                entity.getUserAccount().getEmail(),
                entity.getIntroduction()
        );
    }
}
