package com.lessonmatchingplatform.lesson_matching_platform.account.controller;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentSignupRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentSwitchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorSignUpRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorSwitchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.StudentMyResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorResponse;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/sign-up")
@RestController
public class SignUpController {

    private final SignUpService signUpService;

    // Tutor로 처음 sign up 할 때
    @PostMapping("/tutor")
    public TutorResponse signUpTutor(
            @RequestBody TutorSignUpRequest request
    ) {
        return signUpService.signUpTutor(request);
    }

    // Student로 처음 sign up 할 때
    @PostMapping("/student")
    public StudentMyResponse signUpStudent(
            @RequestBody StudentSignupRequest request
    ) {
        return signUpService.signUpStudent(request);
    }

    // Student로 등록한 경우 Tutor 등록(계정 전환)
    @PostMapping("/tutor-switch")
    public TutorResponse postTutor(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,         // Student로 등록한 계정
            @RequestBody TutorSwitchRequest request
    ) {
        return signUpService.switchTutor(boardPrincipal, request);
    }

    // Tutor로 등록한 경우 Student 등록(계정 전환)
    @PostMapping("/student-switch")
    public StudentMyResponse postStudent(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody StudentSwitchRequest request
    ) {
        return signUpService.switchStudent(boardPrincipal, request);
    }
}

// Student와 Tutor로 각각 회원가입 할 때, 일부 공통된 정보는 UserAccount에 저장하도록