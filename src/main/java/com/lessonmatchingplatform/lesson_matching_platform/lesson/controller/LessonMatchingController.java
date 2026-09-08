package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.PricePerLessonRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsStudent;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
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
@RequestMapping("/api/matchings")
@RestController
public class LessonMatchingController {

    private final LessonMatchingService lessonMatchingService;

    // Student가 Tutor에게 레슨 매칭 요청을 보냄
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/tutors/{tutorId}")
    public ResponseEntity<Long> lessonMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody @Valid LessonMatchingRequest request
    ) {
        Long studentId = boardPrincipal.id();
        Long matchingId = lessonMatchingService.lessonMatching(studentId, tutorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(matchingId);
    }

    // Student가 Tutor에게 보냈던 matching 취소
    @PreAuthorize("hasRole('STUDENT')")
    @PatchMapping("/{matchingId}/cancel")
    public ResponseEntity<Long> cancelMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long matchingId
    ) {
        Long studentId = boardPrincipal.id();
        Long canceledMatchingId = lessonMatchingService.cancelMatching(studentId, matchingId);

        return ResponseEntity.ok().body(canceledMatchingId);
    }

    // Tutor는 자신의 레슨 요청 정보(Matching)들 중 하나를 선택 후, 거절 / 승인 을 답장으로 보냄
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/{matchingId}/status")
    public ResponseEntity<Long> postMyMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long matchingId,
            @RequestBody @Valid LessonStatusRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        Long tutorMatchingId = lessonMatchingService.postMyMatching(tutorId, matchingId, request);

        return ResponseEntity.ok().body(tutorMatchingId);
    }

    // 강사가 학생의 개별적인 레슨비를 설정
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/{matchingId}/price")
    public ResponseEntity<Void> setPricePerLesson(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long matchingId,
            @RequestBody @Valid PricePerLessonRequest request
    ) {
        Long tutorId = boardPrincipal.id();
        lessonMatchingService.setPricePerLesson(tutorId, matchingId, request);

        return ResponseEntity.ok().build();
    }

    // Tutor는 자신에게 들어온 레슨 매칭 요청 목록을 페이징으로 확인
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/tutor/my")
    public ResponseEntity<Page<MyMatchingResponseAsTutor>> myMatchingsAsTutor(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long tutorId = boardPrincipal.id();
        Page<MyMatchingResponseAsTutor> result = lessonMatchingService.myMatchingsAsTutor(tutorId, pageable);
        return ResponseEntity.ok(result);
    }

    // Student는 자신이 보낸 레슨 매칭 요청 목록을 페이징으로 확인
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/my")
    public ResponseEntity<Page<MyMatchingResponseAsStudent>> myMatchingAsStudent(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long studentId = boardPrincipal.id();
        Page<MyMatchingResponseAsStudent> result = lessonMatchingService.myMatchingsAsStudent(studentId, pageable);
        return ResponseEntity.ok(result);
    }
}
