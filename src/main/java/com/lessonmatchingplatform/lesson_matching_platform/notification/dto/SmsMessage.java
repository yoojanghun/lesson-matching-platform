package com.lessonmatchingplatform.lesson_matching_platform.notification.dto;

import lombok.Builder;

/**
 * RabbitMQ SMS 메시지 DTO
 *
 * <p>Producer가 Exchange에 발행하고 Consumer가 수신하는 페이로드.
 * 모든 SMS 이벤트 타입에서 공통으로 사용하며, eventType으로 구분한다.
 *
 * <pre>
 * eventType 예시:
 *   - LESSON_MATCHED       : 레슨 매칭 완료
 *   - PAYMENT_COMPLETED    : 결제 완료
 *   - LESSON_REMINDER      : 레슨 일정 리마인더
 * </pre>
 */
@Builder
public record SmsMessage(

        /** SMS 수신자 전화번호 (예: 010-1234-5678) */
        String phoneNumber,

        /** 발송할 SMS 본문 */
        String content,

        /** 이벤트 유형 (라우팅 키 구분용) */
        String eventType,

        /** 관련 사용자 ID (로깅 / 추적용) */
        Long userId,

        /** 재시도 횟수 (Consumer가 증가시키며 관리) */
        int retryCount
) {
    public static final int MAX_RETRY_COUNT = 3;

    public SmsMessage incrementRetry() {
        return SmsMessage.builder()
                .phoneNumber(this.phoneNumber)
                .content(this.content)
                .eventType(this.eventType)
                .userId(this.userId)
                .retryCount(this.retryCount + 1)
                .build();
    }

    public boolean hasExceededMaxRetry() {
        return this.retryCount >= MAX_RETRY_COUNT;
    }
}
