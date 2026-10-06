package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StudentCancelPaymentRequest(
        @NotBlank(message = "주문 번호(orderId)는 필수입니다.")
        String orderId,

        String cancelReason
) {
}