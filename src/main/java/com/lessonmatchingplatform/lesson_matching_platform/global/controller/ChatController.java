package com.lessonmatchingplatform.lesson_matching_platform.global.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.dto.ChatMessageDto;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.global.service.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;

    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message, BoardPrincipal boardPrincipal) {
        String senderName = boardPrincipal.name();

        ChatMessageDto updatedMessage = ChatMessageDto.builder()
                .type(message.type())
                .matchingId(message.matchingId())
                .sender(senderName)
                .message(message.message())
                .build();

        ChannelTopic topic = new ChannelTopic("chat:room:" + message.matchingId());

        redisPublisher.publish(topic, updatedMessage);
    }
}
