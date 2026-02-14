package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.LessonMatchingResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.service.LessonMatchingService;
import lombok.RequiredArgsConstructor;
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
    public LessonMatchingResponse lessonMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody LessonMatchingRequest request
    ) {
        return lessonMatchingService.lessonMatching(boardPrincipal, tutorId, request);
    }

    // Tutor는 자신의 matching 정보들을 list로 확인
    @PreAuthorize("hasRole('TUTOR')")                       // "ROLE_TUTOR" 권한이 있으면 통과
    @GetMapping("/my/matchings")
    public List<MyMatchingResponse> myMatchings(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        return lessonMatchingService.myMatchings(boardPrincipal);
    }

}
