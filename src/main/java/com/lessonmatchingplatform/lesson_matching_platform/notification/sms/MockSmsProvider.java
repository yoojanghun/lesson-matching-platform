package com.lessonmatchingplatform.lesson_matching_platform.notification.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Mock SMS 공급자 구현체 (로컬/개발/테스트 환경용)
 *
 * <p>실제 SMS API를 호출하지 않고 로그로 출력한다.
 * 운영 환경에서는 실제 SMS 공급자 구현체를 {@code @Primary}로 등록하거나
 * {@code @Profile("prod")}를 통해 교체한다.
 *
 * <p>실패 시뮬레이션:
 * - phoneNumber가 "000"으로 시작하면 {@link SmsException}을 발생시켜
 *   재시도/DLX 흐름을 테스트할 수 있다.
 */
@Slf4j
@Primary
@Component
public class MockSmsProvider implements SmsProvider {

    @Override
    public void send(String phoneNumber, String content) {
        // 테스트용 강제 실패 시뮬레이션
        if (phoneNumber != null && phoneNumber.startsWith("000")) {
            throw new SmsException("Mock: 강제 발송 실패 (테스트용 번호: " + phoneNumber + ")");
        }

        // 실제 SMS 발송 대신 로그 출력
        log.info("[MockSMS] ▶ 발송 성공 | 수신자: {} | 내용: {}", phoneNumber, content);
    }
}
