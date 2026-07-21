package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        Long matchingId = lessonMatchingService.lessonMatching(boardPrincipal, tutorId, request);

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
        Long tutorMatchingId = lessonMatchingService.postMyMatching(boardPrincipal, matchingId, request);

        return ResponseEntity.ok().body(tutorMatchingId);
    }

    // Student가 Tutor와 레슨 매칭이 완료된 후, 특정 시간에 레슨 요청
    @PostMapping("/{tutorId}/matching/{matchingId}")
    public ResponseEntity<Void> requestLessonSchedule(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @PathVariable Long matchingId,
            @RequestBody @Valid LessonScheduleRequest request
    ) {
        lessonMatchingService.lessonScheduleMatching(boardPrincipal, tutorId, matchingId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // TUTOR는 본인이 레슨 가능한 시간을 시간표에서 표시해 둠
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("my/weekly-schedules")
    public ResponseEntity<Void> postMySchedule(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody List<@Valid WeeklyScheduleRequest> request
    ) {
        lessonMatchingService.myScheduleAsTutor(boardPrincipal, request);

        return ResponseEntity.ok().build();
    }

    // TUTOR가 특정 날에 레슨 불가 시간을 신청할 수 있도록 함.
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("my/schedule-exceptions")
    public ResponseEntity<Void> registerScheduleExceptions(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody List<@Valid ScheduleExceptionRequest> request
    ) {
        lessonMatchingService.registerScheduleExceptions(boardPrincipal, request);

        return ResponseEntity.ok().build();
    }

    // 학생이 특정 시간에 레슨 신청하면 해당 레슨을 취소, 삭제, 승인 등을 할 수 있도록 해야 함.
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("my/reservations/{reservationId}/status")
    public ResponseEntity<Void> updateLessonScheduleStatus(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long reservationId,
            @RequestBody @Valid LessonScheduleStatusRequest request
    ) {
        lessonMatchingService.updateLessonScheduleStatus(boardPrincipal, reservationId, request);

        return ResponseEntity.ok().build();
    }

    // 학생이 레슨 신청을 하지 않더라도, 강사는 특정 레슨을 COMPLETED 할 수 있어야 함.
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("my/reservations")
    public ResponseEntity<Void> createDirectReservation(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorDirectReservationRequest request
    ) {
        lessonMatchingService.createDirectReservation(boardPrincipal, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
