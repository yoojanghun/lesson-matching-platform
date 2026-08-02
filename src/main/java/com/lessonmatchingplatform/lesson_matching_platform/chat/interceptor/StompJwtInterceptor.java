package com.lessonmatchingplatform.lesson_matching_platform.chat.interceptor;

import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * WebSocket(STOMP) 연결 시 JWT 인증을 처리하는 인터셉터
 *
 * 동작 방식:
 * 1. 클라이언트가 STOMP CONNECT 프레임을 보낼 때 실행됩니다.
 * 2. STOMP 헤더의 'Authorization: Bearer {token}' 을 추출합니다.
 * 3. JWT 유효성을 검증하고, 인증 객체를 STOMP 세션에 등록합니다.
 * 4. 이후 @MessageMapping 컨트롤러에서 @AuthenticationPrincipal로 사용자 정보를 꺼낼 수 있습니다.
 *
 * 채팅방 구독/퇴장 세션 처리는 WebSocketEventListener가 담당합니다.
 *
 * 클라이언트 연결 예시 (JS SockJS + StompJS):
 * <pre>
 *   const stompClient = new Client({
 *       brokerURL: 'ws://localhost:8080/ws-chat',
 *       connectHeaders: { Authorization: 'Bearer ' + accessToken }
 *   });
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StompJwtInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    // STOMP 세션에 인증 정보 등록 → 이후 @MessageMapping에서 Principal로 접근 가능
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    accessor.setUser(authentication);
                    log.debug("WebSocket JWT 인증 성공: {}", authentication.getName());
                } else {
                    log.warn("WebSocket JWT 인증 실패: 유효하지 않은 토큰");
                    throw new IllegalArgumentException("유효하지 않은 WebSocket JWT 토큰입니다.");
                }
            } else {
                log.warn("WebSocket 연결 시 Authorization 헤더가 없습니다.");
                throw new IllegalArgumentException("WebSocket 연결에 Authorization 헤더가 필요합니다.");
            }
        }

        return message;
    }
}
