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
    private final ChatRoomSessionManager sessionManager;

    // Redis에서 메시지가 Publish되면 MessageListenerAdapter에 의해 이 메서드가 자동 실행됨
    public void sendMessage(String publishMessage) {
        try {
            ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);
            String channelPath = chatMessage.getChannelPath();
            String destination = "/topic/chat/" + channelPath;

            // 수신자 ID 구하기
            Long recipientId = getRecipientId(chatMessage);

            // 수신자가 채팅방에 접속해 있는 지 확인
            boolean isRecipientActive = sessionManager.isUserActiveInRoom(channelPath, recipientId);
            if (!isRecipientActive) {
                sessionManager.incrementUnreadCount(channelPath, recipientId);
            }

            // 1. MongoDB 내역 저장 (저장 실패 시에도 실시간 푸시는 보장)
            ChatMessageDocument savedDocument = null;
            try {
                savedDocument = saveMessageToMongo(chatMessage, isRecipientActive);
            } catch (Exception e) {
                log.error("MongoDB 채팅 메시지 저장 실패: {}", e.getMessage(), e);
            }

            // 기존 객체에 내용 수정
            ChatMessageDto pushMessage = chatMessage.withSender(
                    savedDocument != null ? savedDocument.getId() : null,
                    chatMessage.senderId(),
                    chatMessage.senderName(),
                    isRecipientActive
            );

            // 2. 웹소켓 메시지 푸시
            messagingTemplate.convertAndSend(destination, pushMessage);
            log.info("WebSocket Push 성공 - Destination: {}", destination);
        } catch (Exception e) {
            log.error("RedisSubscriber 메시지 역직렬화/처리 중 에러 발생: {}", e.getMessage(), e);
        }
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
