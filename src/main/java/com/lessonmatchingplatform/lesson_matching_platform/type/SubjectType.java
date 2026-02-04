package com.lessonmatchingplatform.lesson_matching_platform.type;

import lombok.Getter;

@Getter
public enum SubjectType {
    PIANO_CLASSICAL(CategoryType.PIANO),
    PIANO_JAZZ(CategoryType.PIANO),
    PIANO_NEWAGE(CategoryType.PIANO),
    PIANO_POP(CategoryType.PIANO),
    PIANO_CCM(CategoryType.PIANO),

    VIOLIN_CLASSICAL(CategoryType.VIOLIN),
    VIOLIN_JAZZ(CategoryType.VIOLIN),
    VIOLIN_POP(CategoryType.VIOLIN),

    CELLO_CLASSICAL(CategoryType.CELLO),
    CELLO_JAZZ(CategoryType.CELLO),
    CELLO_POP(CategoryType.CELLO),

    GUITAR_CLASSICAL(CategoryType.GUITAR),
    GUITAR_ACOUSTIC(CategoryType.GUITAR), // 통기타 추가
    GUITAR_ELECTRIC(CategoryType.GUITAR),
    GUITAR_BASS(CategoryType.GUITAR),

    DRUM_POP(CategoryType.DRUM),
    DRUM_JAZZ(CategoryType.DRUM),
    DRUM_METAL(CategoryType.DRUM),

    VOCAL_POP(CategoryType.VOCAL),
    VOCAL_JAZZ(CategoryType.VOCAL),
    VOCAL_CLASSICAL(CategoryType.VOCAL),
    VOCAL_MUSICAL(CategoryType.VOCAL),

    COMPOSITION_MIDI(CategoryType.COMPOSITION),
    COMPOSITION_CLASSICAL(CategoryType.COMPOSITION),
    COMPOSITION_JAZZ(CategoryType.COMPOSITION);

    private final CategoryType categoryType;

    SubjectType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
}
