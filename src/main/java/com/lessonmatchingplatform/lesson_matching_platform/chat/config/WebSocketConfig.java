package com.lessonmatchingplatform.lesson_matching_platform.chat.config;

import com.lessonmatchingplatform.lesson_matching_platform.chat.interceptor.StompJwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtInterceptor stompJwtInterceptor;

    /**
     * STOMP 엔드포인트 등록
     * - 클라이언트는 ws://host/ws-chat 으로 WebSocket 연결 시도
     * - SockJS fallback: WebSocket 미지원 환경에서 HTTP 폴링으로 대체
     * - setAllowedOriginPatterns("*"): 개발 편의를 위해 전체 허용 (운영 시 특정 도메인으로 제한)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 메시지 브로커 설정
     * - enableSimpleBroker("/topic", "/queue")
     *     /topic  → 1:N 브로드캐스트 (채팅방 전체 구독)
     *     /queue  → 1:1 메시지 (특정 유저에게 전달)
     * - setApplicationDestinationPrefixes("/app")
     *     클라이언트가 /app/chat/message 로 보내면 @MessageMapping("/chat/message") 컨트롤러가 처리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 클라이언트 인바운드 채널 인터셉터 등록
     * - STOMP CONNECT 프레임 도착 시 StompJwtInterceptor 가 JWT 를 검증합니다.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtInterceptor);
    }
}
