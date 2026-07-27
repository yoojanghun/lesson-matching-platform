package com.lessonmatchingplatform.lesson_matching_platform.global.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lessonmatchingplatform.lesson_matching_platform.global.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.global.dto.ChatMessageDto;
import com.lessonmatchingplatform.lesson_matching_platform.global.repository.ChatMessageMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMongoRepository chatMessageMongoRepository;

    // Redis에서 메시지가 Publish되면 MessageListenerAdapter에 의해 이 메서드가 자동 실행됨
    public void sendMessage(String publishMessage) {
        try {
            ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);

            saveMessageToMongo(chatMessage);

            String destination = "/topic/chat/" + chatMessage.getChannelPath();

            messagingTemplate.convertAndSend(destination, chatMessage);
            log.info("WebSocket Push 성공 - Destination: {}", destination);
        } catch (Exception e) {
            log.error("RedisSubscriber 메시지 처리 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    private void saveMessageToMongo(ChatMessageDto dto) {
        ChatMessageDocument document = ChatMessageDocument.builder()
                .matchingId(dto.matchingId())
                .studentId(dto.studentId())
                .tutorId(dto.tutorId())
                .type(dto.type())
                .sender(dto.sender())
                .message(dto.message())
                .build();

        chatMessageMongoRepository.save(document);
    }
}
