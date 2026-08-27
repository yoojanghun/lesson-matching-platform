package com.lessonmatchingplatform.lesson_matching_platform.account.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LessonType {

    OFFLINE("대면 수업"),
    ONLINE("온라인 수업"),
    BOTH("둘 다 가능");

    private final String description;
}
