package com.lessonmatchingplatform.lesson_matching_platform.account.controller;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.LoginRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TokenResponse;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.AuthService;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인 / 토큰 재발급 / 로그아웃 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "username과 password로 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);

        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken(), 7 * 24 * 60 * 60);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "토큰 재발급", description = "쿠키에 담긴 Refresh Token으로 새 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenResponse tokenResponse = authService.refresh(refreshToken);

        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken(), 7 * 24 * 60 * 60);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal BoardPrincipal principal, HttpServletResponse response) {
        authService.logout(principal.getUsername());

        ResponseCookie cookie = createRefreshTokenCookie("", 0);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)                     // 자바스크립트 접근 차단 (XSS 방어)
                .secure(true)                       // HTTPS 환경에서만 전송
                .path("/")                          // 어떤 주소로 API 요청을 날리든 간에 브라우저가 쿠키를 서버로 보내줌.
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")                 // 외부 사이트에서 우리 서버로 요청을 보낼 때 쿠키 유출, 악용 차단(CSRF 방어)
                .build();
    }
}
