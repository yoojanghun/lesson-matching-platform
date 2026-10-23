package com.lessonmatchingplatform.lesson_matching_platform.notification.producer;

import com.lessonmatchingplatform.lesson_matching_platform.notification.config.RabbitMQConfig;
import com.lessonmatchingplatform.lesson_matching_platform.notification.dto.SmsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SMS 알림 Producer
 *
 * <p>비즈니스 레이어(Service)에서 호출하여 SMS 이벤트를
 * RabbitMQ Topic Exchange로 발행한다.
 *
 * <p>라우팅 키별 큐 매핑:
 * <ul>
 *   <li>{@code sms.lesson.matched}    → sms.lesson.matched.queue</li>
 *   <li>{@code sms.payment.completed} → sms.payment.completed.queue</li>
 *   <li>{@code sms.lesson.reminder}   → sms.lesson.reminder.queue</li>
 * </ul>
 */
@Slf4j
@Component
public class SmsNotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public SmsNotificationProducer(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 레슨 매칭 완료 SMS 발행
     *
     * @param userId      수신 사용자 ID
     * @param phoneNumber 수신자 전화번호
     */
    public void sendLessonMatchedNotification(Long userId, String phoneNumber) {
        String content = "[레슨매칭] 튜터와의 매칭이 완료되었습니다. 레슨 상세 페이지를 확인해주세요.";

        SmsMessage message = SmsMessage.builder()
                .phoneNumber(phoneNumber)
                .content(content)
                .eventType("LESSON_MATCHED")
                .userId(userId)
                .retryCount(0)
                .build();

        publish(RabbitMQConfig.ROUTING_LESSON_MATCHED, message);
    }

    /**
     * 결제 완료 SMS 발행
     *
     * @param userId      수신 사용자 ID
     * @param phoneNumber 수신자 전화번호
     * @param amount      결제 금액
     */
    public void sendPaymentCompletedNotification(Long userId, String phoneNumber, int amount) {
        String content = String.format(
                "[결제완료] %,d원 결제가 완료되었습니다. 감사합니다.", amount);

        SmsMessage message = SmsMessage.builder()
                .phoneNumber(phoneNumber)
                .content(content)
                .eventType("PAYMENT_COMPLETED")
                .userId(userId)
                .retryCount(0)
                .build();

        publish(RabbitMQConfig.ROUTING_PAYMENT_COMPLETED, message);
    }

    /**
     * 레슨 리마인더 SMS 발행
     *
     * @param userId      수신 사용자 ID
     * @param phoneNumber 수신자 전화번호
     * @param lessonTitle 레슨 제목
     * @param hoursLeft   레슨 시작까지 남은 시간
     */
    public void sendLessonReminderNotification(Long userId, String phoneNumber,
                                               String lessonTitle, int hoursLeft) {
        String content = String.format(
                "[레슨알림] '%s' 레슨이 %d시간 후 시작됩니다. 준비해주세요!", lessonTitle, hoursLeft);

        SmsMessage message = SmsMessage.builder()
                .phoneNumber(phoneNumber)
                .content(content)
                .eventType("LESSON_REMINDER")
                .userId(userId)
                .retryCount(0)
                .build();

        publish(RabbitMQConfig.ROUTING_LESSON_REMINDER, message);
    }

    /**
     * Topic Exchange로 메시지 발행 (공통)
     */
    private void publish(String routingKey, SmsMessage message) {
        if (rabbitTemplate == null) {
            log.warn("[SmsProducer] RabbitMQ가 비활성화되어 메시지를 발행하지 않습니다. | routingKey={} | userId={}",
                    routingKey, message.userId());
            return;
        }

        log.info("[SmsProducer] 발행 | routingKey={} | userId={} | phone={}",
                routingKey, message.userId(), message.phoneNumber());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SMS_TOPIC_EXCHANGE,
                routingKey,
                message
        );
    }
}
