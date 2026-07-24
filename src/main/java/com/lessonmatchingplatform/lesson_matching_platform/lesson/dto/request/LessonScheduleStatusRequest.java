package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record LessonScheduleStatusRequest(

        @NotNull(message = "레슨 상태 값은 필수입니다.")
        ReservationStatus reservationStatus
) {

    public LessonScheduleStatusRequest {
        if (reservationStatus != null) {
            if (reservationStatus != ReservationStatus.CONFIRMED &&
                    reservationStatus != ReservationStatus.REJECTED
            ) {
                throw new IllegalArgumentException("튜터가 변경할 수 없는 예약 상태 값입니다.");
            }
        }
    }
}
