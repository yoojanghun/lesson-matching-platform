package com.lessonmatchingplatform.lesson_matching_platform.main.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;

import java.io.Serializable;
import java.util.List;

/**
 * 메인 홈 전체 응답 DTO (Record)
 *
 * Java Record는 final class이므로, Redis ObjectMapper의 DefaultTyping.NON_FINAL 적용 시
 * @class 메타데이터가 누락되어 역직렬화 오류가 발생할 수 있습니다.
 * @JsonTypeInfo를 명시하여 Redis 저장/조회 시 @class 프로퍼티가 항상 안전하게 포함되도록 보장합니다.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record MainHomeResponse(
        List<TutorCardDto> trendingTutors,          // 지금 핫한 레슨 (특정 인기 카테고리/매칭 다수)
        List<TutorCardDto> rookieTutors             // 새로 합류한 루키 (최신 가입순)
) implements Serializable {

    public static MainHomeResponse of(
            List<TutorCardDto> trendingTutors,
            List<TutorCardDto> rookieTutors
    ) {
        return new MainHomeResponse(
                trendingTutors,
                rookieTutors
        );
    }
}
