package com.lessonmatchingplatform.lesson_matching_platform.account.controller;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.AuthTokens;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TokenResponse;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.AuthService;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.SignUpService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/sign-up")
@RestController
public class SignUpController {

        private final SignUpService signUpService;
        private final AuthService authService;

        // Tutor로 처음 sign up 할 때
        @PostMapping("/tutor")
        public ResponseEntity<TokenResponse> signUpTutor(
                        @Valid @RequestBody TutorSignUpRequest request, HttpServletResponse response) {
                signUpService.signUpTutor(request);

                LoginRequest loginRequest = LoginRequest.of(request.userId(), request.userPassword());
                AuthTokens tokens = authService.login(loginRequest);

                ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                TokenResponse tokenResponse = TokenResponse.of(tokens.accessToken(), tokens.expiresIn());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(tokenResponse);
        }

        // GUEST 계정에서 TUTOR로 확정지을 때
        @PostMapping("/tutor-from-guest")
        public ResponseEntity<TokenResponse> signUpTutorFromGuest(
                        @AuthenticationPrincipal BoardPrincipal boardPrincipal,
                        HttpServletResponse response) {
                signUpService.signUpTutorFromGuest(boardPrincipal);

                AuthTokens tokens = authService.issueTokenWithoutPassword(boardPrincipal.username());
                ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                TokenResponse tokenResponse = TokenResponse.of(tokens.accessToken(), tokens.expiresIn());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(tokenResponse);
        }

        // Student로 처음 sign up 할 때
        @PostMapping("/student")
        public ResponseEntity<TokenResponse> signUpStudent(
                        @Valid @RequestBody StudentSignupRequest request,
                        HttpServletResponse response) {
                signUpService.signUpStudent(request);
                LoginRequest loginRequest = LoginRequest.of(request.userId(), request.userPassword());

                AuthTokens tokens = authService.login(loginRequest);
                ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                TokenResponse tokenResponse = TokenResponse.of(tokens.accessToken(), tokens.expiresIn());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(tokenResponse);
        }

        // GUEST 계정에서 STUDENT로 확정지을 때
        @PostMapping("/student-from-guest")
        public ResponseEntity<TokenResponse> signUpStudentFromGuest(
                        @AuthenticationPrincipal BoardPrincipal boardPrincipal,
                        HttpServletResponse response) {
                signUpService.signUpStudentFromGuest(boardPrincipal);

                AuthTokens tokens = authService.issueTokenWithoutPassword(boardPrincipal.username());
                ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                TokenResponse tokenResponse = TokenResponse.of(tokens.accessToken(), tokens.expiresIn());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(tokenResponse);
        }

        // Student로 등록한 경우 Tutor 등록(계정 전환)
        @PostMapping("/tutor-switch")
        public ResponseEntity<TokenResponse> postTutor(
                        @AuthenticationPrincipal BoardPrincipal boardPrincipal,              // Student로 등록한 계정
                        HttpServletResponse response) {
                Long id = boardPrincipal.id();
                signUpService.switchTutor(id);

                AuthTokens tokens = authService.issueTokenWithoutPassword(boardPrincipal.username());
                ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                TokenResponse tokenResponse = TokenResponse.of(tokens.accessToken(), tokens.expiresIn());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(tokenResponse);
        }

        // Tutor로 등록한 경우 Student 등록(계정 전환)
        @PostMapping("/student-switch")
        public ResponseEntity<TokenResponse> postStudent(
                        @AuthenticationPrincipal BoardPrincipal boardPrincipal,
                        HttpServletResponse response) {
                Long id = boardPrincipal.id();
                signUpService.switchStudent(id);

                AuthTokens tokens = authService.issueTokenWithoutPassword(boardPrincipal.username());
                ResponseCookie cookie = createRefreshTokenCookie(tokens.refreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                TokenResponse tokenResponse = TokenResponse.of(tokens.accessToken(), tokens.expiresIn());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(tokenResponse);
        }

        // 중복된 아이디 있는 지 확인하는 로직
        @GetMapping("/check-id")
        public ResponseEntity<Boolean> checkDuplicateId(
                        @RequestParam String userId
        ) {
                boolean isDuplicated = signUpService.checkDuplicateId(userId);

                return ResponseEntity.ok(isDuplicated);
        }

        // 중복된 이메일 있는 지 확인하는 로직
        @GetMapping("/check-email")
        public ResponseEntity<Boolean> checkDuplicateEmail(
                        @RequestParam String email
        ) {
                boolean isDuplicated = signUpService.checkDuplicateEmail(email);

                return ResponseEntity.ok(isDuplicated);
        }

        private ResponseCookie createRefreshTokenCookie(String refreshToken) {
                return ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)                     // 자바스크립트 접근 차단 (XSS 방어)
                        .secure(true)                       // HTTPS 환경에서만 전송
                        .path("/")                          // 어떤 주소로 API 요청을 날리든 간에 브라우저가 쿠키를 서버로 보내줌.
                        .maxAge(604800)
                        .sameSite("Strict")                 // 외부 사이트에서 우리 서버로 요청을 보낼 때 쿠키 유출, 악용 차단(CSRF 방어)
                        .build();
        }
}

// Student와 Tutor로 각각 회원가입 할 때, 일부 공통된 정보는 UserAccount에 저장하도록
// 헤더에 Access Token을 담아서 보내면 JwtAuthenticationFilter가 BoardPrincipal객체로 만들어
// SecurityContextHolder에 저장