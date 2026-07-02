package com.lessonmatchingplatform.lesson_matching_platform.category.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    PIANO("피아노", "🎹"),
    VIOLIN("바이올린", "🎻"),
    CELLO("첼로", "🎻"),
    GUITAR("기타", "🎸"),
    DRUM("드럼", "🥁"),
    VOCAL("보컬", "🎤"),
    COMPOSITION("작곡", "🎼");

    private final String description;
    private final String icon;
}