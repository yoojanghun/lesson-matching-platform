package com.lessonmatchingplatform.lesson_matching_platform.chat.service;

import com.lessonmatchingplatform.lesson_matching_platform.chat.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatReadEventDto;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.request.ChatReadRequest;
import com.lessonmatchingplatform.lesson_matching_platform.chat.repository.ChatMessageMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class ChatReadService {

    private final ChatMessageMongoRepository mongoRepository;
    private final ChatRoomSessionManager sessionManager;
    private final RedisPublisher redisPublisher;

    public void markMessagesAsRead(ChatReadRequest request, Long currentUserId) {
        List<ChatMessageDocument> unreadMessages;
        String channelPath;
        Long requestMatchingId = request.matchingId();
        Long requestStudentId = request.studentId();
        Long requestTutorId = request.tutorId();

        if (requestMatchingId != null) {
            unreadMessages = mongoRepository.findUnreadMatchingMessages(requestMatchingId, currentUserId);
            channelPath = "matching/" + requestMatchingId;
        } else {
            unreadMessages = mongoRepository.findUnreadInquiryMessages(requestStudentId, requestTutorId, currentUserId);
            channelPath = "inquiry/" + requestStudentId + "/" + requestTutorId;
        }

        // 읽지 않은 메세지가 있을 때
        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(ChatMessageDocument::markAsRead);
            mongoRepository.saveAll(unreadMessages);
        }

        // Redis 안 읽은 카운트 0으로 리셋 및 현재 접속 상태 등록
        sessionManager.userEnteredRoom(channelPath, currentUserId);

        // 읽음 이벤트를 Redis Pub/Sub으로 전파 (다중 서버 환경 대응)
        ChatReadEventDto readEvent = ChatReadEventDto.of(channelPath, currentUserId, unreadMessages.size());
        redisPublisher.publishReadEvent(channelPath, readEvent);
    }

    public void leaveRoom(ChatReadRequest request, Long currentUserId) {
        String channelPath;
        if (request.matchingId() != null) {
            channelPath = "matching/" + request.matchingId();
        } else {
            channelPath = "inquiry/" + request.studentId() + "/" + request.tutorId();
        }
        sessionManager.userLeftRoom(channelPath, currentUserId);
    }
}
