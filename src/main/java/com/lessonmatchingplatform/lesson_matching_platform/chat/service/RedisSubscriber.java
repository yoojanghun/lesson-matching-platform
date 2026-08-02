package com.lessonmatchingplatform.lesson_matching_platform.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lessonmatchingplatform.lesson_matching_platform.chat.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatMessageDto;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatReadEventDto;
import com.lessonmatchingplatform.lesson_matching_platform.chat.repository.ChatMessageMongoRepository;
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
    private final ChatRoomSessionManager sessionManager;

    // Redis에서 메시지가 Publish되면 MessageListenerAdapter에 의해 이 메서드가 자동 실행됨
    public void sendMessage(String publishMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(publishMessage);

            // 읽음 이벤트 분기: eventType = "READ_EVENT"
            if (jsonNode.has("eventType")
                    && ChatReadEventDto.READ_EVENT_TYPE.equals(jsonNode.get("eventType").asText())) {
                handleReadEvent(publishMessage);
                return;
            }

            // 일반 채팅 메시지 처리
            handleChatMessage(publishMessage);

        } catch (Exception e) {
            log.error("RedisSubscriber 메시지 처리 중 에러 발생. rawMessage: {}", publishMessage, e);
        }
    }

    /**
     * 읽음 이벤트 처리
     * /topic/chat/{channelPath}/read 로 WebSocket 전파
     */
    private void handleReadEvent(String publishMessage) throws Exception {
        ChatReadEventDto readEvent = objectMapper.readValue(publishMessage, ChatReadEventDto.class);
        String destination = "/topic/chat/" + readEvent.channelPath() + "/read";
        messagingTemplate.convertAndSend(destination, readEvent);
        log.info("읽음 이벤트 WebSocket 전파 - Destination: {}, Reader: {}", destination, readEvent.readerId());
    }

    /**
     * 일반 채팅 메시지 처리
     * MongoDB 저장 → WebSocket 전파 순서로 처리하며,
     * MongoDB 저장 실패 시에도 WebSocket 전파는 반드시 보장합니다.
     */
    private void handleChatMessage(String publishMessage) throws Exception {
        ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);
        String channelPath = chatMessage.getChannelPath();
        String destination = "/topic/chat/" + channelPath;

        // 수신자 ID 계산
        Long recipientId = getRecipientId(chatMessage);

        // 수신자 온라인 여부 확인
        boolean isRecipientActive = sessionManager.isUserActiveInRoom(channelPath, recipientId);
        if (!isRecipientActive) {
            sessionManager.incrementUnreadCount(channelPath, recipientId);
        }

        // 1. MongoDB 저장 (실패해도 WebSocket 전파 보장)
        ChatMessageDocument savedDocument = null;
        try {
            savedDocument = saveMessageToMongo(chatMessage, isRecipientActive);
        } catch (Exception e) {
            log.error("MongoDB 채팅 메시지 저장 실패: {}", e.getMessage(), e);
        }

        // 2. WebSocket 메시지 푸시 (mongoId 반영)
        ChatMessageDto pushMessage = chatMessage.withSender(
                savedDocument != null ? savedDocument.getId() : null,
                chatMessage.senderId(),
                chatMessage.senderName(),
                isRecipientActive
        );
        messagingTemplate.convertAndSend(destination, pushMessage);
        log.info("WebSocket Push 성공 - Destination: {}", destination);
    }

    private Long getRecipientId(ChatMessageDto chatMessage) {
        return chatMessage.senderId().equals(chatMessage.studentId())
                ? chatMessage.tutorId()
                : chatMessage.studentId();
    }

    private ChatMessageDocument saveMessageToMongo(ChatMessageDto dto, boolean isRead) {
        ChatMessageDocument document = ChatMessageDocument.builder()
                .matchingId(dto.matchingId())
                .studentId(dto.studentId())
                .tutorId(dto.tutorId())
                .type(dto.type())
                .senderId(dto.senderId())
                .senderName(dto.senderName())
                .message(dto.message())
                .isRead(isRead)
                .build();

        return chatMessageMongoRepository.save(document);
    }
}
