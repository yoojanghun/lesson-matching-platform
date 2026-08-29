package com.lessonmatchingplatform.lesson_matching_platform.ai.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TutorProfileDto(
        Long tutorId,
        String name,
        GenderType gender,
        String email,
        String introduction,
        List<String> experiences,
        String title,
        List<String> educations,
        Set<String> locations,
        Set<CategoryType> categories,
        Set<String> subjects
) {

    public String toPromptText() {

        String categoriesText = (categories != null && !categories.isEmpty())
                ? categories.stream().map(Enum::name).collect(Collectors.joining(", "))
                : "미지정";

        String locationsText = (locations != null && !locations().isEmpty())
                ? String.join(", ", locations)
                : "미지정";

        String subjectsText = (subjects != null && !subjects.isEmpty())
                ? String.join(", ", subjects)
                : "미지정";

        String genderText = (gender != null) ? gender.name() : "미지정";

        return String.format("""
                [강사 ID: %d | 이름: %s | 성별: %s]
                - 대표 프로필 제목: %s
                - 레슨 가능 지역: %s
                - 카테고리/분야: %s
                - 상세 과목: %s
                - 학력: %s
                - 주요 경력: %s
                - 한줄 소개: %s
                """,
                tutorId,
                name,
                genderText,
                title != null ? title : "",
                locationsText,
                categoriesText,
                subjectsText,
                educations != null ? String.join(", ", educations) : "",
                experiences != null ? String.join(", ", experiences) : "",
                introduction != null ? introduction : ""
        );
    }
}
