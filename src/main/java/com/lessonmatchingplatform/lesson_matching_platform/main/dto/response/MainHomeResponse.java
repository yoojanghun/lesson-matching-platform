package com.lessonmatchingplatform.lesson_matching_platform.main.dto.response;

import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;

import java.util.List;

public record MainHomeResponse(
        List<TutorCardDto> topRatedTutors,          // 수강생 만족도 1위 (평점/리뷰순)
        List<TutorCardDto> trendingTutors,          // 지금 핫한 레슨 (특정 인기 카테고리/매칭 다수)
        List<TutorCardDto> rookieTutors             // 새로 합류한 루키 (최신 가입순)
) {
}
