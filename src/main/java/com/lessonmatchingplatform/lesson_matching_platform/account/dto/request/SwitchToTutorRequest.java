package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.TutorLessonPriceDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SwitchToTutorRequest(
        @NotEmpty(message = "가르칠 악기를 하나 이상 선택해 주세요")
        List<Long> categoryIds,

        @NotEmpty(message = "가르칠 악기의 세부분야를 하나 이상 선택해 주세요")
        List<Long> subjectIds,

        @NotBlank(message = "강사 소개 제목을 작성해 주세요")
        String title,

        @NotBlank(message = "강사 소개글을 작성해 주세요")
        String introduction,

        @NotNull(message = "강사 레슨 유형(대면/온라인)을 입력해 주세요")
        LessonType lessonType,

        List<String> educations,

        List<String> experiences,

        List<Long> locationIds,

        List<Long> styleIds,

        List<Long> goalIds,

        @Valid
        List<TutorLessonPriceDto> lessonPriceDtos,

        String bankName,

        String bankAccountNumber,

        String bankAccountHolder
) implements TutorRegistrable {
}
