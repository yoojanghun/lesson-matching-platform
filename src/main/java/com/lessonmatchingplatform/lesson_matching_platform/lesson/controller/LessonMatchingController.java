package com.lessonmatchingplatform.lesson_matching_platform.lesson.controller;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

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

    // Tutor는 자신의 레슨 요청 정보들 중 하나를 선택후, 거절 / 승인 을 답장으로 보냄
    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/my/matchings/{matchingId}")
    public ResponseEntity<Long> postMyMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long matchingId,
            @RequestBody LessonStatusRequest request
    ) throws AccessDeniedException {
        Long tutorMatchingId = lessonMatchingService.postMyMatching(boardPrincipal, matchingId, request);

        return ResponseEntity.ok().body(tutorMatchingId);
    }

}
