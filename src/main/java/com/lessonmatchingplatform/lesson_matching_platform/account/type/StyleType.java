package com.lessonmatchingplatform.lesson_matching_platform.account.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StyleType {
    KIND_AND_WARM("친절하고 따뜻한 선생님"),
    STRUCTURED_AND_STRICT("체계적이고 엄격한 선생님"),
    FREE_AND_CREATIVE("자유롭고 창의적인 수업"),
    COMMUNICATION_AND_FEEDBACK("소통·피드백 중심"),
    RESULT_AND_SKILL("결과·실력 중심"),
    HUMOROUS_AND_FUN("유머 있고 재미있는 수업"),
    THEORY_AND_PRINCIPLE("이론·원리 설명 중심"),
    ANY("상관없음");

    private final String description;
}