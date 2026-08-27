package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import jakarta.validation.constraints.Pattern;

import java.util.List;

public record TutorProfileRequest(
        @Pattern(
                regexp = "^$|^01[016789]-\\d{3,4}-\\d{4}$",
                message = "전화번호는 010-XXXX-XXXX 형식의 하이픈 포함 올바른 번호여야 합니다."
        )
        String phoneNumber,
        List<Long> styleIds,
        List<Long> categoryIds,
        List<Long> subjectIds,
        List<Long> locationIds,
        String title,
        String career,
        String content,
        String introduction
) {
}
