package com.lessonmatchingplatform.lesson_matching_platform.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CARD("신용/체크카드"),
    EASY_PAY("간편결제"),               // 카카오페이, 토스페이, 네이버페이 등
    VIRTUAL_ACCOUNT("가상계좌"),
    TRANSFER("계좌이체"),
    MOBILE("휴대폰 소액결제");

    private final String description;
}