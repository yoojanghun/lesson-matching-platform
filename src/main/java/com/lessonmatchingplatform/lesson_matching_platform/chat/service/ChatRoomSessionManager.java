package com.lessonmatchingplatform.lesson_matching_platform.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatRoomSessionManager {

    private final RedisTemplate<String, Object> redisTemplate;

    // 안 읽은 카운트 1 증가
    public void incrementUnreadCount(String channelPath, Long recipientId) {
        String unreadKey = "chat:room:unread:" + channelPath;
        redisTemplate.opsForHash().increment(unreadKey, recipientId, 1);
    }

    // 상대방이 현재 채팅방에 접속 중인지 확인
    public boolean isUserActiveInRoom(String channelPath, Long recipientId) {
        String activeKey = "chat:room:active:" + channelPath;
        Boolean isMember = redisTemplate.opsForSet().isMember(activeKey, recipientId);
        return Boolean.TRUE.equals(isMember);
    }

    // 유저가 채팅방에 들어왔을 때 (입장/구독 시)
    public void userEnteredRoom(String channelPath, Long currentUserId) {
        String activeKey = "chat:room:active:" + channelPath;
        redisTemplate.opsForSet().add(activeKey, currentUserId);

        resetUnreadCount(channelPath, currentUserId);
    }

    // 유저가 채팅방에서 나갔을 때 (퇴장/구독해제 시)
    public void userLeftRoom(String channelPath, Long userId) {
        String activeKey = "chat:room:active:" + channelPath;
        redisTemplate.opsForSet().remove(activeKey, userId);
    }

    // 안 읽은 카운트 0으로 초기화
    public void resetUnreadCount(String channelPath, Long userId) {
        String unreadKey = "chat:room:unread:" + channelPath;
        redisTemplate.opsForHash().put(unreadKey, userId, "0");
    }
}
