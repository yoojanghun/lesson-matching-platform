package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import java.math.BigDecimal;

public record ReviewRequest(
        String content,
        BigDecimal rating,
        boolean isAnonymous
) {

}
