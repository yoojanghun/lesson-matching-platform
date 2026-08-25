package com.lessonmatchingplatform.lesson_matching_platform.account.type;

import lombok.Getter;

@Getter
public enum LessonGoalType {
    HOBBY("취미 / 여가"),
    BASIC("기초 다지기"),
    EXAM("입시 / 진학"),
    CERTIFICATE("자격증 취득"),
    SHORT_TERM("단기 성취"),
    CREATION("창작 / 작곡");

    private final String description;

    LessonGoalType(String description) {
        this.description = description;
    }
}

