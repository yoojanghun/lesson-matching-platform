package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TutorProfileUpdateRequest(
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        String title,

        List<String> educations,

        String introduction,

        List<String> experiences,

        List<Long> categoryIds,

        List<Long> subjectIds,

        List<Long> locationIds
) {
}
