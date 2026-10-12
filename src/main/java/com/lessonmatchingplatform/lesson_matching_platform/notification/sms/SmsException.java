package com.lessonmatchingplatform.lesson_matching_platform.notification.sms;

/**
 * SMS 발송 실패 예외
 *
 * <p>Consumer의 재시도 로직을 트리거하는 데 사용된다.
 * 네트워크 오류, API 오류 등 외부 서비스 연동 실패 시 발생시킨다.
 */
public class SmsException extends RuntimeException {

    public SmsException(String message) {
        super(message);
    }

    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
