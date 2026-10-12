package com.lessonmatchingplatform.lesson_matching_platform.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정 클래스
 *
 * <p>Exchange 구조:
 * <pre>
 * [Producer]
 *     └─> sms.topic.exchange (Topic Exchange)
 *             ├─ sms.lesson.matched    --> sms.lesson.matched.queue
 *             ├─ sms.payment.completed --> sms.payment.completed.queue
 *             └─ sms.lesson.reminder   --> sms.lesson.reminder.queue
 *
 * [실패 메시지]
 *     └─> sms.dlx.exchange (Direct Exchange)
 *             └─ sms.#  --> sms.dead-letter.queue (보관/모니터링)
 * </pre>
 *
 * <p>Retry 정책:
 * - 최대 3회 재시도 (지수 백오프: 1s → 2s → 4s)
 * - 3회 초과 시 DLX 큐로 이동
 */
@Configuration
public class RabbitMQConfig {

    // ──────────────────────────────────────────────────────────────────────
    // Exchange 이름 상수
    // ──────────────────────────────────────────────────────────────────────
    public static final String SMS_TOPIC_EXCHANGE   = "sms.topic.exchange";
    public static final String SMS_DLX_EXCHANGE     = "sms.dlx.exchange";

    // ──────────────────────────────────────────────────────────────────────
    // 라우팅 키 상수
    // ──────────────────────────────────────────────────────────────────────
    public static final String ROUTING_LESSON_MATCHED       = "sms.lesson.matched";
    public static final String ROUTING_PAYMENT_COMPLETED    = "sms.payment.completed";
    public static final String ROUTING_LESSON_REMINDER      = "sms.lesson.reminder";

    // ──────────────────────────────────────────────────────────────────────
    // 큐 이름 상수
    // ──────────────────────────────────────────────────────────────────────
    public static final String QUEUE_LESSON_MATCHED      = "sms.lesson.matched.queue";
    public static final String QUEUE_PAYMENT_COMPLETED   = "sms.payment.completed.queue";
    public static final String QUEUE_LESSON_REMINDER     = "sms.lesson.reminder.queue";
    public static final String QUEUE_DEAD_LETTER         = "sms.dead-letter.queue";

    // ──────────────────────────────────────────────────────────────────────
    // Exchange 빈
    // ──────────────────────────────────────────────────────────────────────

    /**
     * 메인 Topic Exchange
     * - 라우팅 키 패턴(sms.lesson.*, sms.payment.*) 기반으로 큐에 메시지를 분배
     */
    @Bean
    public TopicExchange smsTopicExchange() {
        return ExchangeBuilder.topicExchange(SMS_TOPIC_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * Dead Letter Exchange (Direct)
     * - 재시도 횟수 초과 또는 Nack된 메시지가 최종적으로 여기로 이동
     */
    @Bean
    public DirectExchange smsDlxExchange() {
        return ExchangeBuilder.directExchange(SMS_DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────
    // 큐 빈 (DLX 설정 포함)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * 레슨 매칭 완료 SMS 큐
     * - x-dead-letter-exchange: 처리 실패 시 DLX로 이동
     * - x-dead-letter-routing-key: DLX에서 사용할 라우팅 키
     */
    @Bean
    public Queue lessonMatchedQueue() {
        return QueueBuilder.durable(QUEUE_LESSON_MATCHED)
                .withArgument("x-dead-letter-exchange", SMS_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_LESSON_MATCHED)
                .build();
    }

    /**
     * 결제 완료 SMS 큐
     */
    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_COMPLETED)
                .withArgument("x-dead-letter-exchange", SMS_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_PAYMENT_COMPLETED)
                .build();
    }

    /**
     * 레슨 리마인더 SMS 큐
     */
    @Bean
    public Queue lessonReminderQueue() {
        return QueueBuilder.durable(QUEUE_LESSON_REMINDER)
                .withArgument("x-dead-letter-exchange", SMS_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_LESSON_REMINDER)
                .build();
    }

    /**
     * Dead Letter 큐 — 최종 실패 메시지 보관
     * 모니터링 / 수동 재처리 목적
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DEAD_LETTER).build();
    }

    // ──────────────────────────────────────────────────────────────────────
    // 바인딩 (Exchange ↔ Queue)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public Binding lessonMatchedBinding() {
        return BindingBuilder.bind(lessonMatchedQueue())
                .to(smsTopicExchange())
                .with(ROUTING_LESSON_MATCHED);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedQueue())
                .to(smsTopicExchange())
                .with(ROUTING_PAYMENT_COMPLETED);
    }

    @Bean
    public Binding lessonReminderBinding() {
        return BindingBuilder.bind(lessonReminderQueue())
                .to(smsTopicExchange())
                .with(ROUTING_LESSON_REMINDER);
    }

    // DLX 큐에 모든 SMS 이벤트를 하나의 큐로 수집 (라우팅 키별로 따로 보관하려면 분리 가능)
    @Bean
    public Binding deadLetterLessonMatchedBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(smsDlxExchange())
                .with(ROUTING_LESSON_MATCHED);
    }

    @Bean
    public Binding deadLetterPaymentCompletedBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(smsDlxExchange())
                .with(ROUTING_PAYMENT_COMPLETED);
    }

    @Bean
    public Binding deadLetterLessonReminderBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(smsDlxExchange())
                .with(ROUTING_LESSON_REMINDER);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Message Converter & Template
    // ──────────────────────────────────────────────────────────────────────

    /**
     * JSON 메시지 직렬화/역직렬화 컨버터
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate — Producer 전송에 사용
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * ListenerContainerFactory — 수동 Ack 모드 설정
     *
     * <p>AcknowledgeMode.MANUAL: Consumer가 명시적으로 basicAck / basicNack를 호출해야 함.
     * 처리 전 애플리케이션 장애 시 메시지가 큐에 남아 재처리됨 (메시지 손실 방지).
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        // 수동 Ack 모드 — 소비자가 직접 Ack/Nack 호출
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 동시 소비자 수 (기본 1, 필요 시 조정)
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }
}
