package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TutorCancelPaymentRequest(

        @NotNull(message = "주문 번호는 필수입니다.")
        @NotBlank(message = "주문 번호는 필수입니다.")
        String orderId,

        String cancelReason
) {
}