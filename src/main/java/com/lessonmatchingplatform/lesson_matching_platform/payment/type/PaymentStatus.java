package com.lessonmatchingplatform.lesson_matching_platform.payment.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING_TRANSFER("이체 대기"),      // 학생이 결제 요청을 생성했으나 아직 이체 안 함
    TRANSFER_CLAIMED("이체 완료 신고"), // 학생이 이체했다고 신고한 상태, 선생님 확인 대기 중
    DONE("결제 완료"),                  // 선생님이 입금 확인 후 OK 처리 완료
    CANCELLED("결제 취소"),             // 선생님이 금액 불일치 등으로 취소 처리
    EXPIRED("기간 만료");               // 일정 기간 내 이체 신고 없어 만료됨

    private final String description;
}