package com.lessonmatchingplatform.lesson_matching_platform.lesson.domain;

public enum ReservationStatus {
    PENDING,            // 대기중 (학생이 웹으로 신청 후 선생님 승인 대기)
    CONFIRMED,          // 확정 (선생님이 승인했거나, 카톡 건을 직접 수동 등록하여 수업 예정인 상태)
    REJECTED,           // 거절 (선생님이 거절)
    CANCELLED,          // 취소 (확정된 레슨을 수업 시작 전에 취소)
    COMPLETED           // 완료 (실제 수업 시간 종료 후 레슨이 무사히 끝났을 때)
}
