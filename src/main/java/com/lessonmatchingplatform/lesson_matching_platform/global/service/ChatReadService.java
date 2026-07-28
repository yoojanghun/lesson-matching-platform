package com.lessonmatchingplatform.lesson_matching_platform.global.service;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.global.dto.request.ChatReadRequest;
import com.lessonmatchingplatform.lesson_matching_platform.global.repository.ChatMessageMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
public class ChatReadService {

    private final ChatMessageMongoRepository mongoRepository;
    private final ChatRoomSessionManager sessionManager;
    private final SimpMessagingTemplate messagingTemplate;

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

        // 상대방에게 읽음 이벤트 Websocket 전파
        String destination = "/topic/chat/" + channelPath + "/read";
        messagingTemplate.convertAndSend(destination, Map.of(
                "readerId", currentUserId,
                "readCount", unreadMessages.size()
        ));
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
