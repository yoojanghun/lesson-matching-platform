package com.lessonmatchingplatform.lesson_matching_platform.ai.dto;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonGoalType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.StyleType;
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
        LessonType lessonType,
        Set<String> locations,
        Set<CategoryType> categories,
        Set<String> subjects,
        Set<StyleType> styles,
        Set<LessonGoalType> goals
) {

    public static TutorProfileDto from(TutorAccount tutorAccount) {
        Set<CategoryType> categories = tutorAccount.getCategoryTutorSet().stream()
                .map(ct -> ct.getCategory().getName())
                .collect(Collectors.toSet());

        Set<String> subjects = tutorAccount.getSubjectTutorSet().stream()
                .map(st -> st.getSubject().getName().name())
                .collect(Collectors.toSet());

        Set<String> locations = tutorAccount.getLocationTutorSet().stream()
                .map(lt -> lt.getLocation().getName())
                .collect(Collectors.toSet());

        Set<StyleType> styles = tutorAccount.getStyleTutorSet().stream()
                .map(st -> st.getTutorStyle().getStyleType())
                .collect(Collectors.toSet());

        Set<LessonGoalType> goals = tutorAccount.getGoalTutorSet().stream()
                .map(gt -> gt.getLessonGoal().getLessonGoalType())
                .collect(Collectors.toSet());

        return new TutorProfileDto(
                tutorAccount.getTutorId(),
                tutorAccount.getUserAccount() != null ? tutorAccount.getUserAccount().getName() : "",
                tutorAccount.getUserAccount() != null ? tutorAccount.getUserAccount().getGender() : null,
                tutorAccount.getUserAccount() != null ? tutorAccount.getUserAccount().getEmail() : "",
                tutorAccount.getIntroduction(),
                tutorAccount.getExperiences(),
                tutorAccount.getTitle(),
                tutorAccount.getEducations(),
                tutorAccount.getLessonType(),
                locations,
                categories,
                subjects,
                styles,
                goals
        );
    }

    public String toPromptText() {
        String categoriesText = (categories != null && !categories.isEmpty())
                ? categories.stream().map(Enum::name).collect(Collectors.joining(", "))
                : "미지정";

        String locationsText = (locations != null && !locations.isEmpty())
                ? String.join(", ", locations)
                : "미지정";

        String subjectsText = (subjects != null && !subjects.isEmpty())
                ? String.join(", ", subjects)
                : "미지정";

        String stylesText = (styles != null && !styles.isEmpty())
                ? styles.stream().map(s -> s.getDescription() != null ? s.getDescription() : s.name()).collect(Collectors.joining(", "))
                : "미지정";

        String goalsText = (goals != null && !goals.isEmpty())
                ? goals.stream().map(g -> g.getDescription() != null ? g.getDescription() : g.name()).collect(Collectors.joining(", "))
                : "미지정";

        String lessonTypeText = (lessonType != null)
                ? (lessonType.getDescription() != null ? lessonType.getDescription() : lessonType.name())
                : "미지정";

        String genderText = (gender != null) ? gender.name() : "미지정";

        return String.format("""
                [강사 ID: %d | 이름: %s | 성별: %s]
                - 대표 프로필 제목: %s
                - 수업 방식: %s
                - 레슨 가능 지역: %s
                - 카테고리/분야: %s
                - 상세 과목: %s
                - 레슨 스타일/성향: %s
                - 레슨 목표 대상: %s
                - 학력: %s
                - 주요 경력: %s
                - 한줄 소개: %s
                """,
                tutorId,
                name,
                genderText,
                title != null ? title : "",
                lessonTypeText,
                locationsText,
                categoriesText,
                subjectsText,
                stylesText,
                goalsText,
                educations != null ? String.join(", ", educations) : "",
                experiences != null ? String.join(", ", experiences) : "",
                introduction != null ? introduction : ""
        );
    }
}
