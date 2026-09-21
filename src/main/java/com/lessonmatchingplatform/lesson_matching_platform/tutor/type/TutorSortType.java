package com.lessonmatchingplatform.lesson_matching_platform.tutor.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TutorSortType {
    RECOMMENDED("인기순"),                // 인기순
    LATEST("최신순");                     // 최신 등록 순

    private final String description;
}
