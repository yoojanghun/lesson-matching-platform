package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.ScheduleExceptionRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.WeeklyScheduleRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.TutorScheduleResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@RestController
public class ScheduleController {

    private final LessonMatchingService lessonMatchingService;

    // Student/공통: Tutor가 레슨 가능한 일정을 확인하는 요청
    @GetMapping("/tutors/{tutorId}")
    public ResponseEntity<TutorScheduleResponse> getTutorSchedules(
            @PathVariable Long tutorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        TutorScheduleResponse tutorScheduleResponse = lessonMatchingService.getTutorSchedules(tutorId, startDate, endDate);

        return ResponseEntity.ok().body(tutorScheduleResponse);
    }

    // TUTOR: 본인이 레슨 가능한 정기 주간 일정을 등록/수정
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/my/weekly")
    public ResponseEntity<Void> postMySchedule(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody List<@Valid WeeklyScheduleRequest> request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.myScheduleAsTutor(tutorId, request);

        return ResponseEntity.ok().build();
    }

    // TUTOR: 특정 날짜의 휴무/추가 가능 등 예외 일정을 등록
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/my/exceptions")
    public ResponseEntity<Void> registerScheduleExceptions(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody List<@Valid ScheduleExceptionRequest> request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.registerScheduleExceptions(tutorId, request);

        return ResponseEntity.ok().build();
    }
}
