package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossApproveResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        String requestedAt,
        String approvedAt,
        String type,
        Integer totalAmount,
        String method
) {
}
