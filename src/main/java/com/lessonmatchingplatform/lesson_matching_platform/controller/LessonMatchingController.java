package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.LessonMatchingResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.service.LessonMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/tutors")              // 자원의 계층 구조를 명확히 함(tutors)
@RestController
public class LessonMatchingController {

    private final LessonMatchingService lessonMatchingService;

    @PostMapping("/{tutorId}/matching")
    public LessonMatchingResponse lessonMatching(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable Long tutorId,
            @RequestBody LessonMatchingRequest request
    ) {
        return lessonMatchingService.lessonMatching(boardPrincipal, tutorId, request);
    }

}
