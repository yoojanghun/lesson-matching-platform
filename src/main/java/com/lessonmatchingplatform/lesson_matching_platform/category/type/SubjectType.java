package com.lessonmatchingplatform.lesson_matching_platform.category.type;

import lombok.Getter;

@Getter
public enum SubjectType {
    PIANO_CLASSICAL(CategoryType.PIANO, "클래식"),
    PIANO_JAZZ(CategoryType.PIANO, "재즈"),
    PIANO_NEWAGE(CategoryType.PIANO, "뉴에이지"),
    PIANO_POP(CategoryType.PIANO, "팝"),
    PIANO_CCM(CategoryType.PIANO, "CCM"),

    VIOLIN_CLASSICAL(CategoryType.VIOLIN, "클래식"),
    VIOLIN_JAZZ(CategoryType.VIOLIN, "재즈"),
    VIOLIN_POP(CategoryType.VIOLIN, "팝"),

    CELLO_CLASSICAL(CategoryType.CELLO, "클래식"),
    CELLO_JAZZ(CategoryType.CELLO, "재즈"),
    CELLO_POP(CategoryType.CELLO, "팝"),

    GUITAR_CLASSICAL(CategoryType.GUITAR, "클래식"),
    GUITAR_ACOUSTIC(CategoryType.GUITAR, "어쿠스틱"), // 통기타 추가
    GUITAR_ELECTRIC(CategoryType.GUITAR, "일렉기타"),
    GUITAR_BASS(CategoryType.GUITAR, "베이스"),

    DRUM_POP(CategoryType.DRUM, "팝"),
    DRUM_JAZZ(CategoryType.DRUM, "재즈"),
    DRUM_METAL(CategoryType.DRUM, "메탈"),

    VOCAL_POP(CategoryType.VOCAL, "팝"),
    VOCAL_JAZZ(CategoryType.VOCAL, "재즈"),
    VOCAL_CLASSICAL(CategoryType.VOCAL, "클래식"),
    VOCAL_MUSICAL(CategoryType.VOCAL, "뮤지컬"),

    COMPOSITION_MIDI(CategoryType.COMPOSITION, "미디"),
    COMPOSITION_CLASSICAL(CategoryType.COMPOSITION, "클래식"),
    COMPOSITION_JAZZ(CategoryType.COMPOSITION, "재즈");

    private final CategoryType categoryType;
    private final String description;

    SubjectType(CategoryType categoryType, String description) {
        this.categoryType = categoryType;
        this.description = description;
    }
}
