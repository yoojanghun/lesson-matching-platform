package com.lessonmatchingplatform.lesson_matching_platform.account.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.StyleTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.SubjectTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.TutorLessonPriceDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import java.time.LocalDate;
import java.util.List;

public record TutorProfileResponse(
                String name,
                GenderType gender,
                LocalDate birthDate,
                String email,
                String phoneNumber,
                Boolean isBirthDatePublic,
                Boolean isEmailPublic,
                Boolean isPhoneNumberPublic,
                String title,
                String content,
                String introduction,
                String career,
                List<LocationDto> locations,
                List<CategoryTypeDto> categories,
                List<SubjectTypeDto> subjects,
                List<StyleTypeDto> styles,
                List<GoalTypeDto> goals,
                List<TutorLessonPriceDto> prices) {

        public static TutorProfileResponse of(
                        TutorAccount entity,
                        List<CategoryTypeDto> categories,
                        List<SubjectTypeDto> subjects,
                        List<LocationDto> locations,
                        List<StyleTypeDto> styles,
                        List<GoalTypeDto> goals,
                        List<TutorLessonPriceDto> prices) {
                return new TutorProfileResponse(
                                entity.getUserAccount().getName(),
                                entity.getUserAccount().getGender(),
                                entity.getUserAccount().getBirthDate(),
                                entity.getUserAccount().getEmail(),
                                entity.getUserAccount().getPhoneNumber(),
                                entity.getIsBirthDatePublic(),
                                entity.getIsEmailPublic(),
                                entity.getIsPhoneNumberPublic(),
                                entity.getTitle(),
                                entity.getContent(),
                                entity.getIntroduction(),
                                entity.getCareer(),
                                locations,
                                categories,
                                subjects,
                                styles,
                                goals,
                                prices
                );
        }
}
