package com.lessonmatchingplatform.lesson_matching_platform.global.security.oauth2;

import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtProperties;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String REDIRECT_URL = "http://localhost:3000/oauth2/redirect"; // 임시 프론트엔드 주소

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getUserId();                                 // UserAccount's userId
        List<String> roles = oAuth2User.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtTokenProvider.createAccessToken(userId, roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // Refresh Token → HttpOnly 쿠키로 전달 (URL 노출 방지)
        // OAuth2는 Google → 서버 → 프론트 cross-site 리다이렉트이므로 sameSite=Lax 사용
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)                 // 자바스크립트 접근 차단 (XSS 방어)
                .secure(true)                   // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(7 * 24 * 60 * 60)      // 7일
                .sameSite("Lax")               // cross-site 리다이렉트 허용 (Strict는 OAuth2에서 쿠키 차단됨)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        boolean isGuest = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_GUEST"));

        // Access Token + isGuest만 URL에 담기 (Refresh Token 제거)
        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URL)
                .queryParam("accessToken", accessToken)
                .queryParam("isGuest", isGuest)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
