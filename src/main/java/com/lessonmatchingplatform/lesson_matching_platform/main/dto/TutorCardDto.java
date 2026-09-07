package com.lessonmatchingplatform.lesson_matching_platform.main.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.SubjectTypeDto;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonGoalType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document.TutorDocument;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public record TutorCardDto(
        Long tutorId,
        String name,
        String title,
        List<GoalTypeDto> goalTypeDtoList,
        List<CategoryTypeDto> categoryTypeDtoList,
        List<SubjectTypeDto> subjectTypeDtoList,
        TutorLessonPriceRangeDto priceRange,
        Integer reviewCount,
        BigDecimal averageRating
) {
    public static TutorCardDto of(
            TutorAccount tutorAccount,
            List<GoalTypeDto> goalTypeDtoList,
            List<CategoryTypeDto> categoryTypeDtoList,
            List<SubjectTypeDto> subjectTypeDtoList,
            TutorLessonPriceRangeDto priceRange
    ) {
        return new TutorCardDto(
                tutorAccount.getTutorId(),
                tutorAccount.getUserAccount().getName(),
                tutorAccount.getTitle(),
                goalTypeDtoList,
                categoryTypeDtoList,
                subjectTypeDtoList,
                priceRange,
                tutorAccount.getReviewCount(),
                tutorAccount.getAverageRating()
        );
    }

    public static TutorCardDto from(TutorDocument doc) {
        List<GoalTypeDto> goalDtos = new ArrayList<>();
        if (doc.getGoals() != null && doc.getGoalIds() != null) {
            int size = Math.min(doc.getGoals().size(), doc.getGoalIds().size());
            for (int i = 0; i < size; i++) {
                LessonGoalType type = LessonGoalType.fromNullable(doc.getGoals().get(i));
                if (type != null) {
                    goalDtos.add(new GoalTypeDto(doc.getGoalIds().get(i), type));
                } else {
                    log.warn("매핑 실패한 GoalTypeDto: {}", doc.getGoals().get(i));
                }
            }
        }

        List<CategoryTypeDto> categoryDtos = new ArrayList<>();
        if (doc.getCategories() != null && doc.getCategoryIds() != null) {
            int size = Math.min(doc.getCategories().size(), doc.getCategoryIds().size());
            for (int i = 0; i < size; i++) {
                CategoryType type = CategoryType.fromNullable(doc.getCategories().get(i));
                if (type != null) {
                    categoryDtos.add(new CategoryTypeDto(doc.getCategoryIds().get(i), type));
                } else {
                    log.warn("매핑 실패한 categoryDtos: {}", doc.getCategories().get(i));
                }
            }
        }

        List<SubjectTypeDto> subjectDtos = new ArrayList<>();
        if (doc.getSubjects() != null && doc.getSubjectIds() != null) {
            int size = Math.min(doc.getSubjects().size(), doc.getSubjectIds().size());
            for (int i = 0; i < size; i++) {
                SubjectType type = SubjectType.fromNullable(doc.getSubjects().get(i));
                if (type != null) {
                    subjectDtos.add(new SubjectTypeDto(doc.getSubjectIds().get(i), type));
                } else {
                    log.warn("매핑 실패한 subjectDtos: {}", doc.getSubjects().get(i));
                }
            }
        }

        TutorLessonPriceRangeDto priceRange = TutorLessonPriceRangeDto.of(doc.getMinPrice(), doc.getMaxPrice());

        return new TutorCardDto(
                doc.getId(),
                doc.getName(),
                doc.getTitle(),
                goalDtos,
                categoryDtos,
                subjectDtos,
                priceRange,
                doc.getReviewCount() != null ? doc.getReviewCount() : 0,
                doc.getAverageRating() != null ? doc.getAverageRating() : BigDecimal.ZERO
        );
    }
}
