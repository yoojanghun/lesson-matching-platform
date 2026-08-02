package com.lessonmatchingplatform.lesson_matching_platform.chat.listener;

import com.lessonmatchingplatform.lesson_matching_platform.chat.service.ChatRoomSessionManager;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final ChatRoomSessionManager sessionManager;

    /**
     * 유저가 특정 채팅방을 구독(입장)했을 때
     * - sessionAttributes에 userId, channelPath 저장 (Disconnect/Unsubscribe 시 참조)
     * - 온라인 상태 등록 (안읽음 카운트는 ChatReadService.markMessagesAsRead에서 리셋)
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        // 읽음 이벤트 구독 채널(/topic/chat/.../read)은 세션 처리 제외
        if (destination == null
                || !destination.startsWith("/topic/chat/")
                || destination.endsWith("/read")) {
            return;
        }

        String channelPath = destination.substring("/topic/chat/".length());
        Authentication auth = (Authentication) accessor.getUser();

        if (auth != null && auth.getPrincipal() instanceof BoardPrincipal principal) {
            Long userId = principal.id();

            // Disconnect/Unsubscribe 시 사용할 수 있도록 세션에 저장
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put("userId", userId);
                sessionAttributes.put("channelPath", channelPath);
            }

            sessionManager.userEnteredRoom(channelPath, userId);
            log.info("채팅방 입장 - 유저 ID: {}, 채널: {}", userId, channelPath);
        }
    }

    /**
     * 유저가 채팅방 구독을 해제(퇴장/페이지 이동)했을 때
     */
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        clearSession(accessor);
    }

    /**
     * 유저의 WebSocket 연결이 완전히 끊어졌을 때 (브라우저 종료, 네트워크 단절 등)
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        clearSession(accessor);
    }

    private void clearSession(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) return;

        Object userIdObj = sessionAttributes.get("userId");
        Object channelPathObj = sessionAttributes.get("channelPath");

        if (userIdObj != null && channelPathObj != null) {
            Long userId = (Long) userIdObj;
            String channelPath = channelPathObj.toString();

            sessionManager.userLeftRoom(channelPath, userId);

            // 사용 후 Attribute 정리
            sessionAttributes.remove("userId");
            sessionAttributes.remove("channelPath");

            log.info("채팅방 퇴장 처리 - 유저 ID: {}, 채널: {}", userId, channelPath);
        }
    }
}
