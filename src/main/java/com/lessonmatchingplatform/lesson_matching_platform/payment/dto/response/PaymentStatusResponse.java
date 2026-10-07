package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentStatusResponse(
        Long paymentId,
        String orderId,
        Integer amount,
        Integer lessonCount,
        PaymentStatus paymentStatus,
        String cancelReason,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime transferClaimedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime confirmedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static PaymentStatusResponse of(Payment entity) {
        return new PaymentStatusResponse(
                entity.getPaymentId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getLessonCount(),
                entity.getPaymentStatus(),
                entity.getCancelReason(),
                entity.getTransferClaimedAt(),
                entity.getConfirmedAt(),
                entity.getCreatedAt()
        );
    }
}