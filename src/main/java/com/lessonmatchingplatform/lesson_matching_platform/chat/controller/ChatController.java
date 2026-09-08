package com.lessonmatchingplatform.lesson_matching_platform.chat.controller;

import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatMessageDto;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.chat.service.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;

    @MessageMapping("/chat/message")        // @MessageMapping: Websocket메세지를 수신하여 특정 메서드로 연결해줌
    public void message(
            ChatMessageDto message,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        if (boardPrincipal == null) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }

        Long senderId = boardPrincipal.id();
        String senderName = boardPrincipal.name();

        // 발신자가 해당 채팅방의 학생 또는 튜터인지 검증 (제3자 메시지 위조 방지)
        if (message.studentId() == null || message.tutorId() == null ||
                (!senderId.equals(message.studentId()) && !senderId.equals(message.tutorId()))) {
            throw new IllegalArgumentException("해당 채팅방의 참가자만 메시지를 보낼 수 있습니다.");
        }

        ChatMessageDto updatedMessage = message.withSender(
                null,
                senderId,
                senderName,
                false,
                LocalDateTime.now()
        );

        ChannelTopic topic = new ChannelTopic("chat:room:" + updatedMessage.getChannelPath());
        redisPublisher.publish(topic, updatedMessage);
    }
}

