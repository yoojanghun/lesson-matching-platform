package com.lessonmatchingplatform.lesson_matching_platform.chat.dto;

/**
 * Redis Pub/Sub을 통해 읽음 이벤트를 전파할 때 사용하는 페이로드 DTO
 *
 * RedisSubscriber가 수신 메시지의 eventType을 보고
 * 일반 채팅 메시지와 읽음 이벤트를 구분합니다.
 *
 * 전파 흐름:
 *  ChatReadService → RedisPublisher.publishReadEvent()
 *  → Redis chat:room:{channelPath}
 *  → RedisSubscriber.sendMessage()
 *  → STOMP /topic/chat/{channelPath}/read
 */
public record ChatReadEventDto(
        String eventType,       // 고정값: "READ_EVENT" (RedisSubscriber 분기용)
        String channelPath,     // 전파 목적지 (ex. "matching/10", "inquiry/1/2")
        Long readerId,          // 읽음을 수행한 유저 ID
        int readCount           // 읽음 처리된 메시지 수
) {
    public static final String READ_EVENT_TYPE = "READ_EVENT";

    public static ChatReadEventDto of(String channelPath, Long readerId, int readCount) {
        return new ChatReadEventDto(READ_EVENT_TYPE, channelPath, readerId, readCount);
    }
}
