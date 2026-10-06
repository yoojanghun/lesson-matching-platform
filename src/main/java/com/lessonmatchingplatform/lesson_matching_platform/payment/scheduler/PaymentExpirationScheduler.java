package com.lessonmatchingplatform.lesson_matching_platform.payment.scheduler;

import com.lessonmatchingplatform.lesson_matching_platform.payment.service.LessonPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final LessonPaymentService lessonPaymentService;

    // 매시 정각마다 24시간 초과된 미입금 건 자동 만료 처리
    @Scheduled(cron = "0 0 * * * *")
    public void expirePendingPayments() {
        try {
            lessonPaymentService.expireTimeoutPayments();
        } catch (Exception e) {
            log.error("미입금 만료 스케줄러 실행 중 오류 발생", e);
        }
    }
}