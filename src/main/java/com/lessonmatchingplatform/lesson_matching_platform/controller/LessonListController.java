package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponseAsStudent;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.service.LessonMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class LessonListController {

    private final LessonMatchingService lessonMatchingService;

    // Tutor는 자신의 레슨 요청 정보들을 list로 확인
    @PreAuthorize("hasRole('TUTOR')")                       // "ROLE_TUTOR" 권한이 있으면 통과
    @GetMapping("/tutor/my/matchings")
    public List<MyMatchingResponseAsTutor> myMatchingsAsTutor(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        return lessonMatchingService.myMatchingsAsTutor(boardPrincipal);
    }

    // Student는 자신이 보낸 레슨 요청 정보들을 List로 확인
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/my/matchings")
    public List<MyMatchingResponseAsStudent> myMatchingAsStudent(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        return lessonMatchingService.myMatchingsAsStudent(boardPrincipal);
    }
}
