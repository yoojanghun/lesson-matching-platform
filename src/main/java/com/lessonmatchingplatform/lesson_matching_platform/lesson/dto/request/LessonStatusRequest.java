package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;
import jakarta.validation.constraints.NotNull;

public record LessonStatusRequest(

        @NotNull(message = "상태값은 필수입니다.")
        MatchingStatus status
) {
    public LessonStatusRequest {
        // NPE 방지를 위해 status != null 선행 체크
        if (status != null && status == MatchingStatus.PENDING) {
            throw new IllegalArgumentException("상태값으로 PENDING을 지정할 수 없습니다.");
        }
    }
}