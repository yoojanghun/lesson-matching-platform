package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentMethod;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentConfirmResponse(
        Long paymentId,                         // 결제 PK
        String orderId,                         // 주문 번호
        Integer amount,                         // 최종 승인된 결제 금액
        PaymentMethod paymentMethod,            // 선택된 결제 수단 (CARD, EASY_PAY 등)
        PaymentStatus paymentStatus,            // 결제 상태 (DONE)
        LocalDateTime approvedAt                // PG사 최종 승인 일시
) {
    public static PaymentConfirmResponse of(Payment entity) {
        return new PaymentConfirmResponse(
                entity.getPaymentId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getPaymentStatus(),
                entity.getApprovedAt()
        );
    }
}
