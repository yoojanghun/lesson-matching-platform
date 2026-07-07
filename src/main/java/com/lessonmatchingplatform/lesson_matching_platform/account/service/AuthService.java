package com.lessonmatchingplatform.lesson_matching_platform.account.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.LoginRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TokenResponse;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtProperties;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;

    // 로그인 → Access Token + Refresh Token 발급
    public TokenResponse login(LoginRequest request) {
        // Spring Security로 인증 처리 (비밀번호 검증 포함)
        Authentication authentication = authenticationManager.authenticate(                         // DB 유저 정보, 비밀번호 일치 확인
                new UsernamePasswordAuthenticationToken(request.username(), request.password())     // 미인증 Authentication
        );

        String username = authentication.getName();
        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        // Redis에 Refresh Token 저장 (Key: "refresh:{username}", TTL: 7일) => 서버 RAM에 저장
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + username,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // Refresh Token으로 Access Token 재발급 (RTR 방식)
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        String redisKey = REFRESH_TOKEN_PREFIX + username;

        // 서버 Redis에 저장된 Refresh Token과 비교
        Object storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.toString().equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않거나 만료되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(username);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

        redisTemplate.opsForValue().set(
                redisKey,
                newRefreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return TokenResponse.of(newAccessToken, newRefreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // 로그아웃 → Redis에서 Refresh Token 삭제
    public void logout(String username) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    }
}
