package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;

public record PaymentPrepareResponse(

        String orderId,             // 백엔드가 생성한 고유 주문 번호
        Integer amount,             // 실제 계산 금액
        String orderName            // PG사 결제창에 표시될 주문 명
) {
    public static PaymentPrepareResponse of(Payment entity, String orderName) {
        return new PaymentPrepareResponse(
                entity.getOrderId(),
                entity.getAmount(),
                orderName
        );
    }
}
