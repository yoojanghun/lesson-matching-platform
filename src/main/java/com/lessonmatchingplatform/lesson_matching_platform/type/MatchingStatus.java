package com.lessonmatchingplatform.lesson_matching_platform.type;

public enum MatchingStatus {
    PENDING,            // 대기중
    ACCEPTED,           // 승인
    REJECTED,           // 거절
    CANCELLED,          // 취소: 승인 후 학생이나 선생님이 매칭을 취소
}