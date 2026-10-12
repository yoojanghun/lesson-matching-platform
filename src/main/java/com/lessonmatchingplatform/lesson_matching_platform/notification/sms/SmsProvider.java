package com.lessonmatchingplatform.lesson_matching_platform.notification.sms;

/**
 * SMS 발송 공급자 추상화 인터페이스
 *
 * <p>실제 SMS 서비스(Twilio, NHN Cloud, CoolSMS 등)와 독립적으로
 * 비즈니스 로직을 구성할 수 있도록 추상화한다.
 *
 * <p>구현체 교체 전략:
 * <ul>
 *   <li>{@link MockSmsProvider} — 로컬/테스트 환경 (로그 출력)</li>
 *   <li>실제 구현체 — 운영 환경 (@Profile("prod") 적용)</li>
 * </ul>
 */
public interface SmsProvider {

    /**
     * SMS 발송
     *
     * @param phoneNumber 수신자 전화번호
     * @param content     SMS 본문
     * @throws SmsException 발송 실패 시 (재시도 트리거용)
     */
    void send(String phoneNumber, String content);
}
