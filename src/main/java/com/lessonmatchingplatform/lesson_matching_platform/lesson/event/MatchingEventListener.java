package com.lessonmatchingplatform.lesson_matching_platform.lesson.event;

import com.lessonmatchingplatform.lesson_matching_platform.notification.producer.SmsNotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingEventListener {

    private final SmsNotificationProducer smsNotificationProducer;

    /**
     * DB 트랜잭션이 성공적으로 커밋된(AFTER_COMMIT) 직후,
     * 별도의 비동기 스레드(@Async)에서 실행되어 HTTP 응답 속도를 최적화하고
     * Tomcat 스레드 점유 및 DB 커넥션 유지 시간을 최소화합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchingAccepted(MatchingAcceptedEvent event) {
        log.info("[Async] 매칭 승인 트랜잭션 커밋 완료 - SMS 알림 비동기 발행 (studentId: {}, thread: {})",
                event.studentId(), Thread.currentThread().getName());
        smsNotificationProducer.sendLessonMatchedNotification(event.studentId(), event.phoneNumber());
    }
}
