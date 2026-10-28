package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Redis 캐싱 시 SliceImpl의 Jackson 기본 생성자 부재 및 역직렬화 실패를 방지하기 위한 캐시 DTO.
 *
 * GenericJackson2JsonRedisSerializer 사용 시 SliceImpl 직렬화/역직렬화 오류를 차단하고,
 * Spring Data Slice로 즉시 변환할 수 있도록 지원합니다.
 */
public record CachedReviewSliceDto(
        List<ReviewResponse> content,
        boolean hasNext
) implements Serializable {

    public static CachedReviewSliceDto from(Slice<ReviewResponse> slice) {
        if (slice == null) {
            return new CachedReviewSliceDto(Collections.emptyList(), false);
        }
        return new CachedReviewSliceDto(slice.getContent(), slice.hasNext());
    }

    public Slice<ReviewResponse> toSlice(Pageable pageable) {
        return new SliceImpl<>(content != null ? content : Collections.emptyList(), pageable, hasNext);
    }
}

