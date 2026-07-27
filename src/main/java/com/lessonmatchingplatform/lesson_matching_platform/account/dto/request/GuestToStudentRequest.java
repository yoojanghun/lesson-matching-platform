package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import java.time.LocalDate;

public record GuestToStudentRequest(
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String introduction
) {
}
