package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record TutorDirectReservationRequest(

        @NotNull(message = "매칭 ID는 필수입니다.")
        Long matchingId,

        @NotNull(message = "레슨 날짜는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotNull(message = "시작 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        String requestMsg,

        @NotNull(message = "레슨 상태는 필수입니다.")
        ReservationStatus status
) {

    public TutorDirectReservationRequest {
        if (status != null && date != null && startTime != null && endTime != null) {
            if (status == ReservationStatus.COMPLETED) {
                if (date.isAfter(LocalDate.now()) || (date().isEqual(LocalDate.now()) && endTime().isAfter(LocalTime.now()))) {
                    throw new IllegalStateException("미래의 레슨 일정은 완료(COMPLETED) 상태로 직접 등록할 수 없습니다.");
                }
            }
            if (status != ReservationStatus.COMPLETED && status != ReservationStatus.CONFIRMED) {
                throw new IllegalStateException("강사가 직접 등록할 수 없는 레슨 상태 값입니다.");
            }
        }
    }
}
