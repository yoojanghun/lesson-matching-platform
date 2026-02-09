package com.lessonmatchingplatform.lesson_matching_platform.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;

import java.time.LocalDate;

public record StudentSignupRequest(
        String userId,
        String userPassword,
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String email,
        String introduction
) {

}
