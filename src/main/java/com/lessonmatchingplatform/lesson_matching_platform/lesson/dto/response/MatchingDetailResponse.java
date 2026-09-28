package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MatchingDetailResponse(
        Long matchingId,
        String requestMsg,
        MatchingStatus status,
        Integer pricePerLesson,
        String studentName,
        GenderType studentGender,
        LocalDate studentBirthDate,
        String studentPhoneNumber,
        String studentEmail,
        String tutorName,
        LocalDateTime createdAt
) {
    public static MatchingDetailResponse from(Matching entity) {
        UserAccount studentUserAccount = entity.getStudentAccount().getUserAccount();
        UserAccount tutorUserAccount = entity.getTutorAccount().getUserAccount();

        return new MatchingDetailResponse(
                entity.getMatchingId(),
                entity.getRequestMsg(),
                entity.getStatus(),
                entity.getPricePerLesson(),
                studentUserAccount.getName(),
                studentUserAccount.getGender(),
                studentUserAccount.getBirthDate(),
                studentUserAccount.getPhoneNumber(),
                studentUserAccount.getEmail(),
                tutorUserAccount.getName(),
                entity.getCreatedAt()
        );
    }
}
