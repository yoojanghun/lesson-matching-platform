package com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationResponse(
        Long reservationId,

        Long studentId,

        String studentName,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate lessonDate,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        ReservationStatus reservationStatus,

        Integer pricePerLesson,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt // 신청 시각
) {
    public static ReservationResponse of(Reservation entity) {
        return new ReservationResponse(
                entity.getReservationId(),
                entity.getMatching().getStudentAccount().getStudentId(),
                entity.getMatching().getStudentAccount().getUserAccount().getName(),
                entity.getLessonDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getReservationStatus(),
                entity.getMatching().getPricePerLesson(),
                entity.getCreatedAt()
        );
    }
}
