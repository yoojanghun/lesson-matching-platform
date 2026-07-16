package com.lessonmatchingplatform.lesson_matching_platform.account.controller;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TokenResponse;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.AuthService;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final AuthService authService;

    // Tutor로 처음 sign up 할 때
    @PostMapping("/tutor")
    public ResponseEntity<TokenResponse> signUpTutor(
            @RequestBody TutorSignUpRequest request
    ) {
        signUpService.signUpTutor(request);
        LoginRequest loginRequest = LoginRequest.of(request.userId(), request.userPassword());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.login(loginRequest));
    }

    // GUEST 계정에서 TUTOR로 확정지을 때
    @PostMapping("/tutor-from-guest")
    public ResponseEntity<TokenResponse> signUpTutorFromGuest(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody GuestToTutorRequest request
    ) {
        signUpService.signUpTutorFromGuest(boardPrincipal, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.issueTokenWithoutPassword(boardPrincipal.username()));
    }

    // Student로 처음 sign up 할 때
    @PostMapping("/student")
    public ResponseEntity<TokenResponse> signUpStudent(
            @RequestBody StudentSignupRequest request
    ) {
        signUpService.signUpStudent(request);
        LoginRequest loginRequest = LoginRequest.of(request.userId(), request.userPassword());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.login(loginRequest));
    }

    @PostMapping("/student-from-guest")
    public ResponseEntity<TokenResponse> signUpStudentFromGuest(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody GuestToStudentRequest request
    ) {
        signUpService.signUpStudentFromGuest(boardPrincipal, request);
    }

    // Student로 등록한 경우 Tutor 등록(계정 전환)
    @PostMapping("/tutor-switch")
    public ResponseEntity<TokenResponse> postTutor(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,         // Student로 등록한 계정
            @RequestBody TutorSwitchRequest request
    ) {
        signUpService.switchTutor(boardPrincipal, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.issueTokenWithoutPassword(boardPrincipal.username()));
    }

    // Tutor로 등록한 경우 Student 등록(계정 전환)
    @PostMapping("/student-switch")
    public ResponseEntity<TokenResponse> postStudent(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody StudentSwitchRequest request
    ) {
        signUpService.switchStudent(boardPrincipal, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.issueTokenWithoutPassword(boardPrincipal.username()));
    }
}

// Student와 Tutor로 각각 회원가입 할 때, 일부 공통된 정보는 UserAccount에 저장하도록
// 헤더에 Access Token을 담아서 보내면 JwtAuthenticationFilter가 BoardPrincipal객체로 만들어 SecurityContextHolder에 저장