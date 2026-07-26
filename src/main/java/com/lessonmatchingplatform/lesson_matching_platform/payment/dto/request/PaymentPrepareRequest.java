package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentPrepareRequest(

        @NotNull(message = "매칭 ID는 필수입니다.")
        Long matchingId,

        @NotNull(message = "결제할 레슨 회차/시간 정보는 필수입니다.")
        @Positive(message = "1 이상의 올바른 레슨 회차/시간 정보를 입력해 주세요.")
        Integer lessonCount
) {
}
