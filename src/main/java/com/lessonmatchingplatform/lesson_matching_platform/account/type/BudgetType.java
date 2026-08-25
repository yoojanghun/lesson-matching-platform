package com.lessonmatchingplatform.lesson_matching_platform.account.type;

import lombok.Getter;

@Getter
public enum BudgetType {
    UNDER_50K("5만원 이하 / 회"),
    BETWEEN_50K_70K("5~7만원 / 회"),
    BETWEEN_70K_100K("7~10만원 / 회"),
    OVER_100K("10만원 이상 / 회"),
    NEGOTIABLE("상관없음");

    private final String description;

    BudgetType(String description) {
        this.description = description;
    }
}
