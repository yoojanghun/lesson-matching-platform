package com.lessonmatchingplatform.lesson_matching_platform.account.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.InterestCategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.StyleTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.BudgetType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;

import java.time.LocalDate;
import java.util.List;

public record StudentProfileResponse(
        String name,
        GenderType gender,
        LocalDate birthDate,
        String email,
        String phoneNumber,
        List<StyleTypeDto> styles,
        List<InterestCategoryTypeDto> instruments,
        List<GoalTypeDto> goals,
        List<LocationDto> locations,
        String introduction,
        String lessonType,
        BudgetType budgetType
) {

    public static StudentProfileResponse of(
            StudentAccount studentAccount,
            List<StyleTypeDto> styles,
            List<InterestCategoryTypeDto> instruments,
            List<GoalTypeDto> goals,
            List<LocationDto> locations
    ) {
        UserAccount user = studentAccount.getUserAccount();

        return new StudentProfileResponse(
                user.getName(),
                user.getGender(),
                user.getBirthDate(),
                user.getEmail(),
                user.getPhoneNumber(),
                styles,
                instruments,
                goals,
                locations,
                studentAccount.getIntroduction(),
                studentAccount.getLessonType(),
                studentAccount.getBudgetType()
        );
    }
}
