package com.lessonmatchingplatform.lesson_matching_platform.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;

import java.time.LocalDate;
import java.util.Set;

public record TutorSignUpRequest(
        String userId,
        String userPassword,
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String email,
        String introduction,
        String career,
        String title,
        String content,
        Set<Long> categoryId,
        Set<Long> subjectId,
        Set<Long> locationId
) {

}
