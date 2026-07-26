package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(

        @NotNull(message = "paymentKey는 필수입니다.")
        String paymentKey,                              // PG사가 발급한 결제 건별 고유 승인 키

        @NotNull(message = "orderId는 필수입니다.")
        String orderId,                                 // 백엔드가 /prepare에서 생성했던 주문 번호

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "올바른 결제 금액을 입력해 주세요.")
        Integer amount                                  // 클라이언트가 결제했다고 주장하는 금액 (백엔드 DB 금액과 대조 검증용)
) {
}
