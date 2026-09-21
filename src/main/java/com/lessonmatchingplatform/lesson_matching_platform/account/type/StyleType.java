package com.lessonmatchingplatform.lesson_matching_platform.account.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StyleType {
    KIND_AND_WARM("친절하고 따뜻한"),
    STRUCTURED_AND_STRICT("체계적이고 엄격한"),
    FREE_AND_CREATIVE("자유롭고 창의적인"),
    COMMUNICATION_AND_FEEDBACK("소통 중심"),
    RESULT_AND_SKILL("결과 중심"),
    HUMOROUS_AND_FUN("유머 있고 재미있는"),
    THEORY_AND_PRINCIPLE("이론 설명 중심"),
    ANY("상관없음");

    private final String description;
}