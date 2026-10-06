package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PaymentPrepareRequest(
        @NotNull(message = "매칭 ID는 필수입니다.")
        Long matchingId,

        @NotNull(message = "예약 ID는 필수입니다.")
        List<Long> reservationId
) {
}