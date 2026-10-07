package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;

public record PaymentPrepareResponse(
        String orderId,
        Integer amount,
        Integer lessonCount,
        String orderName,
        PaymentStatus paymentStatus,
        String tutorBankName,
        String tutorBankAccountNumber,
        String tutorBankAccountHolder
) {
    public static PaymentPrepareResponse of(Payment entity, String orderName) {
        return new PaymentPrepareResponse(
                entity.getOrderId(),
                entity.getAmount(),
                entity.getLessonCount(),
                orderName,
                entity.getPaymentStatus(),
                entity.getTutorBankName(),
                entity.getTutorBankAccountNumber(),
                entity.getTutorBankAccountHolder()
        );
    }
}