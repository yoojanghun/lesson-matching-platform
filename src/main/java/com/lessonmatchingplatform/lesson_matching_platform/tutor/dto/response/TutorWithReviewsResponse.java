package com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GenderType;
import org.springframework.data.domain.Slice;

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
        Set<CategoryType> categories,
        Set<String> subjects,
        Slice<ReviewResponse> reviews                // 최신순 정렬을 위해 필요
) {

    public static TutorWithReviewsResponse from(TutorAccount entity, Slice<ReviewResponse> reviews) {
        UserAccount userAccount = entity.getUserAccount();

        Set<String> locations = entity.getLocationTutorSet().stream()
                .map(locationTutor ->
                        locationTutor.getLocation().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<CategoryType> categories = entity.getCategoryTutorSet().stream()
                .map(categoryTutor ->
                        categoryTutor.getCategory().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> subjects = entity.getSubjectTutorSet().stream()
                .map(subjectTutor ->
                        subjectTutor.getSubject().getName())
                .collect(Collectors.toUnmodifiableSet());

        Set<Matching> matchings = entity.getMatchingSet();

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
