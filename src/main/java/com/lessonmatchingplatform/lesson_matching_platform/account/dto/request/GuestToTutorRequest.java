package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GenderType;
import java.time.LocalDate;
import java.util.Set;

public record GuestToTutorRequest(
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String introduction,
        String career,
        String title,
        String content,
        Set<Long> categoryId,
        Set<Long> subjectId,
        Set<Long> locationId
) {
}
