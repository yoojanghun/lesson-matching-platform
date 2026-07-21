package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReservationResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.TutorScheduleResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/tutors")              // 자원의 계층 구조를 명확히 함(tutors)
@RestController
public class LessonMatchingController {

    private final LessonMatchingService lessonMatchingService;

    // Student가 Tutor에게 레슨 요청을 보냄
    @PostMapping("/{tutorId}/matching")
    public ResponseEntity<Long> lessonMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody LessonMatchingRequest request
    ) {
        Long studentId = boardPrincipal.id();
        Long matchingId = lessonMatchingService.lessonMatching(studentId, tutorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(matchingId);
    }

    // Tutor는 자신의 레슨 요청 정보(Matching)들 중 하나를 선택후, 거절 / 승인 을 답장으로 보냄
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/my/matchings/{matchingId}")
    public ResponseEntity<Long> postMyMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long matchingId,
            @RequestBody @Valid LessonStatusRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        Long tutorMatchingId = lessonMatchingService.postMyMatching(tutorId, matchingId, request);

        return ResponseEntity.ok().body(tutorMatchingId);
    }

    // Student가 Tutor가 레슨 가능한 일정을 확인하는 요청
    @GetMapping("/{tutorId}/schedules")
    public ResponseEntity<TutorScheduleResponse> getTutorSchedules(
            @PathVariable Long tutorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        TutorScheduleResponse tutorScheduleResponse = lessonMatchingService.getTutorSchedules(tutorId, startDate, endDate);

        return ResponseEntity.ok().body(tutorScheduleResponse);
    }

    // Student가 Tutor와 레슨 매칭이 완료된 후, 특정 시간에 레슨 요청
    @PostMapping("/{tutorId}/matching/{matchingId}")
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

    // TUTOR는 본인이 레슨 가능한 시간을 시간표에서 표시해 둠
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("my/weekly-schedules")
    public ResponseEntity<Void> postMySchedule(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody List<@Valid WeeklyScheduleRequest> request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.myScheduleAsTutor(tutorId, request);

        return ResponseEntity.ok().build();
    }

    // TUTOR가 특정 날에 레슨 불가 시간을 신청할 수 있도록 함.
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("my/schedule-exceptions")
    public ResponseEntity<Void> registerScheduleExceptions(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody List<@Valid ScheduleExceptionRequest> request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.registerScheduleExceptions(tutorId, request);

        return ResponseEntity.ok().build();
    }

    // TUTOR는 자신에게 요청이 들어온 Reservation들을 Page 형태로 확인할 수 있도록 해야 함.
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("my/reservations")
    public ResponseEntity<Page<ReservationResponse>> getTutorReservations(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long tutorId = boardPrincipal.id();
        Page<ReservationResponse> reservations = lessonMatchingService.getTutorReservations(tutorId, status, pageable);

        return ResponseEntity.ok().body(reservations);
    }

    // 학생이 특정 시간에 레슨 신청하면 해당 레슨을 취소, 삭제, 승인 등을 할 수 있도록 해야 함.
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("my/reservations/{reservationId}/status")
    public ResponseEntity<Void> updateLessonScheduleStatus(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long reservationId,
            @RequestBody @Valid LessonScheduleStatusRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.updateLessonScheduleStatus(tutorId, reservationId, request);

        return ResponseEntity.ok().build();
    }

    // 학생이 레슨 신청을 하지 않더라도, 강사는 특정 레슨을 COMPLETED 할 수 있어야 함.
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("my/reservations")
    public ResponseEntity<Void> createDirectReservation(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorDirectReservationRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.createDirectReservation(tutorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
