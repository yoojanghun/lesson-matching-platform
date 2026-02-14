package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponseAsStudent;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.service.LessonMatchingService;
import lombok.RequiredArgsConstructor;
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
    public MyMatchingResponseAsStudent lessonMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody LessonMatchingRequest request
    ) {
        return lessonMatchingService.lessonMatching(boardPrincipal, tutorId, request);
    }

    // Tutor는 자신의 레슨 요청 정보들 중 하나를 선택후, 거절 / 승인 을 답장으로 보냄
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/my/matchings/{matchingId}")
    public MyMatchingResponseAsTutor postMyMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long matchingId,
            @RequestBody LessonStatusRequest request
    ) throws AccessDeniedException {
        return lessonMatchingService.postMyMatching(boardPrincipal, matchingId, request);
    }

}
