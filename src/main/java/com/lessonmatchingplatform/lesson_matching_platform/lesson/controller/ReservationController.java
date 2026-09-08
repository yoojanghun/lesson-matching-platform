package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonScheduleRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonScheduleStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.TutorDirectReservationRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReservationResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.ReservationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
@RestController
public class ReservationController {

    private final LessonMatchingService lessonMatchingService;

    // Student가 Tutor와 레슨 매칭이 완료된 후, 특정 시간에 레슨 예약 신청
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/matchings/{matchingId}/tutors/{tutorId}")
    public ResponseEntity<Void> requestLessonSchedule(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @PathVariable Long matchingId,
            @RequestBody @Valid LessonScheduleRequest request
    ) {
        Long studentId = boardPrincipal.id();
        lessonMatchingService.lessonScheduleMatching(studentId, tutorId, matchingId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Student가 Tutor에게 보냈던 reservation 취소
    @PreAuthorize("hasRole('STUDENT')")
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<Long> cancelReservation(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long reservationId
    ) {
        Long studentId = boardPrincipal.id();
        Long canceledReservationId = lessonMatchingService.cancelReservation(studentId, reservationId);

        return ResponseEntity.ok().body(canceledReservationId);
    }

    // TUTOR는 자신에게 요청이 들어온 Reservation들을 Page 형태로 확인할 수 있음
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/my")
    public ResponseEntity<Page<ReservationResponse>> getTutorReservations(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long tutorId = boardPrincipal.id();
        Page<ReservationResponse> reservations = lessonMatchingService.getTutorReservations(tutorId, status, pageable);

        return ResponseEntity.ok().body(reservations);
    }

    // 학생이 특정 시간에 레슨 신청하면 강사가 해당 레슨을 취소, 거절, 승인 등을 처리
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/{reservationId}/status")
    public ResponseEntity<Void> updateLessonScheduleStatus(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long reservationId,
            @RequestBody @Valid LessonScheduleStatusRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.updateLessonScheduleStatus(tutorId, reservationId, request);

        return ResponseEntity.ok().build();
    }

    // 학생이 레슨 신청을 하지 않더라도, 강사는 특정 레슨을 직접 생성/완료(COMPLETED) 처리 가능
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/direct")
    public ResponseEntity<Void> createDirectReservation(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorDirectReservationRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.createDirectReservation(tutorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
