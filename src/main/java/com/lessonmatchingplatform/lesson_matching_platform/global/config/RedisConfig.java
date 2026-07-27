package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;                    // redis 서버 주소(localhost)

    @Value("${spring.data.redis.port}")
    private int port;                       // redis 포트 번호(port)

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(RedisSerializer.json());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                .entryTtl(Duration.ofHours(1));                 // 캐시 유효 시간 (1시간)

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Redis Pub/Sub: 메시지 리스너 컨테이너
     * - RedisSubscriber가 채널을 구독하면, 발행된 메시지를 여기서 수신해 처리합니다.
     * - 채널 등록은 ChatService 등에서 동적으로 추가할 수 있습니다.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Redis Pub/Sub: 메시지 리스너 어댑터
     * - RedisSubscriber의 onMessage 메서드를 리스너로 등록합니다.
     * - RedisSubscriber 구현 후 주입받아 사용하세요.
     *   (예: container.addMessageListener(listenerAdapter, new PatternTopic("chat:*")))
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
        // RedisSubscriber 구현 후 아래처럼 변경:
        // return new MessageListenerAdapter(redisSubscriber, "onMessage");
        return new MessageListenerAdapter(new Object() {
            public void onMessage(String message) {}
        });
    }
}
