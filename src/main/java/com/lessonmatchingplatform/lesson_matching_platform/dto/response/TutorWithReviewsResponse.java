package com.lessonmatchingplatform.lesson_matching_platform.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record TutorWithReviewsResponse(
        Long tutorId,
        String name,
        GenderType gender,
        String email,
        String introduction,
        String career,
        String title,
        String content,
        Set<String> locations,
        Set<String> categories,
        Set<String> subjects,
        List<ReviewResponse> reviews                // 최신순 정렬을 위해 필요
) {

    public static TutorWithReviewsResponse from(TutorAccount entity) {
        UserAccount userAccount = entity.getUserAccount();

        Set<String> locations = entity.getLocationTutorSet().stream()
                .map(locationTutor ->
                        locationTutor.getLocation().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> categories = entity.getCategoryTutorSet().stream()
                .map(categoryTutor ->
                        categoryTutor.getCategory().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<Matching> matchings = entity.getMatchingSet();

        List<ReviewResponse> reviews = matchings.stream()
                .map(Matching::getLessonReview)         // matching에서 review 가져옴
                .filter(Objects::nonNull)               // review가 null이 아닌 것 가져옴
                .map(ReviewResponse::from)              // review -> ReviewResponse
                .toList();

        return new TutorWithReviewsResponse(
                entity.getTutorId(),
                userAccount.getName(),
                userAccount.getGender(),
                userAccount.getEmail(),
                entity.getIntroduction(),
                entity.getCareer(),
                entity.getTitle(),
                entity.getContent(),
                locations,
                categories,
                subjects,
                reviews
        );
    }
}
