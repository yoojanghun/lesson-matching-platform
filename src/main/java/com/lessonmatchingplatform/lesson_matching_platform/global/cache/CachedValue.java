package com.lessonmatchingplatform.lesson_matching_platform.global.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 논리 TTL (Soft TTL) 및 최대 보장 만료(Hard TTL) 적용을 위한 캐시 래퍼 클래스
 *
 * [Jackson 주의사항]
 * - @JsonIgnoreProperties(ignoreUnknown = true): 알 수 없는 필드 역직렬화 실패 방지
 * - isExpired(), isHardExpired()에 @JsonIgnore 필수 (getter로 인식되어 직렬화되는 현상 차단)
 * - final 클래스 및 record 금지 (DefaultTyping.NON_FINAL 호환)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class CachedValue<T> implements Serializable {

    private T data;
    private long softExpireAt; // 논리 만료 시각 (epoch ms)
    private long hardExpireAt; // 최대 허용 수명 시각 (epoch ms) - 쿨다운 시에도 연장되지 않고 불변 유지

    @JsonIgnore
    public boolean isExpired() {
        return System.currentTimeMillis() >= softExpireAt;
    }

    @JsonIgnore
    public boolean isHardExpired() {
        return System.currentTimeMillis() >= hardExpireAt;
    }
}
