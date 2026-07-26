package com.lessonmatchingplatform.lesson_matching_platform.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    READY("결제 대기"),              // /prepare 호출 후 PG 결제창 진입 전 상태
    IN_PROGRESS("결제 진행중"),      // PG사 카드 인증 성공 후 백엔드 confirm 승인 대기 중
    DONE("결제 완료"),               // PG사 최종 승인 완료 및 DB 처리 완료
    FAILED("결제 실패"),            // 잔액 부족, 위변조 감지, 타임아웃 등으로 결제 실패
    CANCELLED("결제 취소");         // 결제 완료 후 전체/부분 환불(취소)된 상태

    private final String description;
}