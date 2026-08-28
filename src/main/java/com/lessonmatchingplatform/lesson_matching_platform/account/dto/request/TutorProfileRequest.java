package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.TutorLessonPriceDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record TutorProfileRequest(
        String name,
        LocalDate birthDate,
        String email,
        Boolean isBirthDatePublic,
        Boolean isEmailPublic,
        Boolean isPhoneNumberPublic,
        @Pattern(
                regexp = "^$|^01[016789]-\\d{3,4}-\\d{4}$",
                message = "전화번호는 010-XXXX-XXXX 형식의 하이픈 포함 올바른 번호여야 합니다."
        )
        String phoneNumber,
        
        @Size(max = 3, message = "레슨 가격은 최대 3개까지만 설정할 수 있습니다.")
        List<TutorLessonPriceDto> prices,
        
        List<Long> styleIds,
        List<Long> goalIds,
        List<Long> categoryIds,
        List<Long> subjectIds,
        List<Long> locationIds,
        String title,
        String career,
        String content,
        String introduction
) {
}
