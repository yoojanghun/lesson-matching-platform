package com.lessonmatchingplatform.lesson_matching_platform.payment.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    BANK_TRANSFER("계좌이체 (P2P 직접 이체)");

    private final String description;
}