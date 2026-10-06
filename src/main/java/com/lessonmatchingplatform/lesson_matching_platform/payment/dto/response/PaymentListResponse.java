package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentListResponse(
        Long paymentId,
        String orderId,
        String counterpartName,
        Integer amount,
        Integer lessonCount,
        PaymentStatus paymentStatus,
        String cancelReason,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime transferClaimedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime confirmedAt
) {
}