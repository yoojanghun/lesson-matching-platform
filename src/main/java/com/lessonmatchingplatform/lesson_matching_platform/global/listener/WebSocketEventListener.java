package com.lessonmatchingplatform.lesson_matching_platform.global.listener;

import com.lessonmatchingplatform.lesson_matching_platform.global.service.ChatRoomSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final ChatRoomSessionManager sessionManager;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String channelPath = (String) sessionAttributes.get("channelPath");
            Long userId = (Long) sessionAttributes.get("userId");

            if (channelPath != null && userId != null) {
                sessionManager.userLeftRoom(channelPath, userId);
                log.info("WebSocket 세션 종료로 인한 퇴장 처리 - 유저 ID: {}, 채널: {}", userId, channelPath);
            }
        }
    }
}
