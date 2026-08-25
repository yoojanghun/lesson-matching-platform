package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.BudgetType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonGoalType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.StyleType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StudentProfileRequest(
        @Pattern(
                regexp = "^$|^01[016789]-\\d{3,4}-\\d{4}$",
                message = "전화번호는 010-XXXX-XXXX 형식의 하이픈 포함 올바른 번호여야 합니다."
        )
        String phoneNumber,

        List<StyleType> styles,
        List<CategoryType> instruments,
        List<LessonGoalType> goals,
        List<Long> locationIds,

        @Size(max = 2000, message = "자기소개는 2000자 이내로 입력해 주세요.")
        String introduction,

        String lessonType,
        BudgetType budgetType
) {
}
