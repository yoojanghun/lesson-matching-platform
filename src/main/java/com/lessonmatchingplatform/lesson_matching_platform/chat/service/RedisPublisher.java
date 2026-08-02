package com.lessonmatchingplatform.lesson_matching_platform.chat.service;

import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatMessageDto;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatReadEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChannelTopic topic, ChatMessageDto message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }

    /**
     * 읽음 이벤트를 Redis Pub/Sub으로 발행합니다.
     * RedisSubscriber가 eventType = "READ_EVENT"를 보고 분기 처리합니다.
     */
    public void publishReadEvent(String channelPath, ChatReadEventDto readEvent) {
        String channelName = "chat:room:" + channelPath;
        redisTemplate.convertAndSend(channelName, readEvent);
    }
}

