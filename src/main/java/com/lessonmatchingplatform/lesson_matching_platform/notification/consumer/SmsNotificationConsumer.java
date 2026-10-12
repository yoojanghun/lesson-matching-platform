package com.lessonmatchingplatform.lesson_matching_platform.notification.consumer;

import com.lessonmatchingplatform.lesson_matching_platform.notification.config.RabbitMQConfig;
import com.lessonmatchingplatform.lesson_matching_platform.notification.dto.SmsMessage;
import com.lessonmatchingplatform.lesson_matching_platform.notification.sms.SmsException;
import com.lessonmatchingplatform.lesson_matching_platform.notification.sms.SmsProvider;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * SMS 알림 Consumer
 *
 * <p>각 큐별로 @RabbitListener를 등록하여 메시지를 소비한다.
 *
 * <h2>수동 Ack/Nack 처리 전략</h2>
 * <ul>
 *   <li>성공: {@code channel.basicAck} → 메시지를 큐에서 제거</li>
 *   <li>실패 + 재시도 가능: 지수 백오프 대기 후 동일 큐에 재발행 + {@code basicAck}</li>
 *   <li>실패 + 재시도 한계 초과: {@code basicNack(requeue=false)} → DLX 큐로 이동</li>
 * </ul>
 *
 * <h2>재시도 전략 (지수 백오프)</h2>
 * <pre>
 * 1회차: 1초 대기 후 재시도
 * 2회차: 2초 대기 후 재시도
 * 3회차: 4초 대기 후 재시도
 * 4회차 이상: DLX 큐로 이동 (포기)
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true")
public class SmsNotificationConsumer {

    private static final long BASE_RETRY_DELAY_MS = 1000L; // 기본 지연: 1초

    private final SmsProvider smsProvider;
    private final RabbitTemplate rabbitTemplate;

    // ──────────────────────────────────────────────────────────────────────
    // 레슨 매칭 완료 Consumer
    // ──────────────────────────────────────────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LESSON_MATCHED,
                    containerFactory = "rabbitListenerContainerFactory")
    public void consumeLessonMatched(SmsMessage message, Channel channel,
                                     Message rawMessage) throws IOException {
        processWithRetry(message, channel, rawMessage, RabbitMQConfig.ROUTING_LESSON_MATCHED);
    }

    // ──────────────────────────────────────────────────────────────────────
    // 결제 완료 Consumer
    // ──────────────────────────────────────────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_COMPLETED,
                    containerFactory = "rabbitListenerContainerFactory")
    public void consumePaymentCompleted(SmsMessage message, Channel channel,
                                        Message rawMessage) throws IOException {
        processWithRetry(message, channel, rawMessage, RabbitMQConfig.ROUTING_PAYMENT_COMPLETED);
    }

    // ──────────────────────────────────────────────────────────────────────
    // 레슨 리마인더 Consumer
    // ──────────────────────────────────────────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LESSON_REMINDER,
                    containerFactory = "rabbitListenerContainerFactory")
    public void consumeLessonReminder(SmsMessage message, Channel channel,
                                      Message rawMessage) throws IOException {
        processWithRetry(message, channel, rawMessage, RabbitMQConfig.ROUTING_LESSON_REMINDER);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Dead Letter Consumer (모니터링 / 알람 목적)
    // ──────────────────────────────────────────────────────────────────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DEAD_LETTER,
                    containerFactory = "rabbitListenerContainerFactory")
    public void consumeDeadLetter(SmsMessage message, Channel channel,
                                  Message rawMessage) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        log.error("[DLQ] SMS 최종 실패 | userId={} | phone={} | eventType={} | retryCount={} | content={}",
                message.userId(), message.phoneNumber(),
                message.eventType(), message.retryCount(), message.content());

        // DLQ 메시지는 Ack하여 큐에서 제거 (필요 시 DB 저장 / 알람 연동 가능)
        channel.basicAck(deliveryTag, false);
    }

    // ──────────────────────────────────────────────────────────────────────
    // 공통 재시도 로직
    // ──────────────────────────────────────────────────────────────────────

    /**
     * SMS 발송 처리 + 수동 Ack/Nack + 지수 백오프 재시도
     *
     * @param message     수신한 SMS 메시지
     * @param channel     AMQP 채널 (Ack/Nack 호출용)
     * @param rawMessage  원본 AMQP 메시지 (deliveryTag 추출용)
     * @param routingKey  재발행 시 사용할 라우팅 키
     */
    private void processWithRetry(SmsMessage message, Channel channel,
                                  Message rawMessage, String routingKey) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();

        try {
            log.info("[SmsConsumer] 수신 | routingKey={} | userId={} | phone={} | retryCount={}",
                    routingKey, message.userId(), message.phoneNumber(), message.retryCount());

            // SMS 발송 시도
            smsProvider.send(message.phoneNumber(), message.content());

            // ✅ 성공: 메시지를 큐에서 제거
            channel.basicAck(deliveryTag, false);
            log.info("[SmsConsumer] ✅ Ack 완료 | userId={} | phone={}",
                    message.userId(), message.phoneNumber());

        } catch (SmsException e) {
            log.warn("[SmsConsumer] ⚠ SMS 발송 실패 | userId={} | phone={} | retryCount={} | 원인: {}",
                    message.userId(), message.phoneNumber(), message.retryCount(), e.getMessage());

            // 재시도 횟수 초과 여부 확인
            if (message.hasExceededMaxRetry()) {
                // ❌ 한계 초과: basicNack(requeue=false) → DLX 큐로 이동
                log.error("[SmsConsumer] ❌ 최대 재시도 초과 → DLX로 이동 | userId={} | phone={}",
                        message.userId(), message.phoneNumber());
                channel.basicNack(deliveryTag, false, false);
            } else {
                // 🔄 재시도: 지수 백오프 대기 후 같은 큐에 재발행
                applyExponentialBackoff(message.retryCount());
                SmsMessage retryMessage = message.incrementRetry();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SMS_TOPIC_EXCHANGE,
                        routingKey,
                        retryMessage
                );

                // 원본 메시지는 Ack로 제거 (재발행한 메시지가 별도로 소비됨)
                channel.basicAck(deliveryTag, false);
                log.info("[SmsConsumer] 🔄 재발행 완료 | retryCount={} | userId={}",
                        retryMessage.retryCount(), retryMessage.userId());
            }

        } catch (Exception e) {
            // 예상치 못한 예외: 즉시 DLX로 이동 (애플리케이션 오류)
            log.error("[SmsConsumer] 🚨 예상치 못한 오류 → DLX로 이동 | userId={} | 원인: {}",
                    message.userId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 지수 백오프 대기
     *
     * <p>재시도 횟수에 따라 대기 시간이 기하급수적으로 증가:
     * <pre>
     * retryCount=0: 1초
     * retryCount=1: 2초
     * retryCount=2: 4초
     * </pre>
     *
     * @param retryCount 현재 재시도 횟수 (0-indexed)
     */
    private void applyExponentialBackoff(int retryCount) {
        long delayMs = BASE_RETRY_DELAY_MS * (long) Math.pow(2, retryCount);
        log.info("[SmsConsumer] 지수 백오프 대기 중: {}ms (retryCount={})", delayMs, retryCount);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[SmsConsumer] 백오프 대기 중 인터럽트 발생");
        }
    }
}
