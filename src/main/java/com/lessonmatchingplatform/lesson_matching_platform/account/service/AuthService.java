package com.lessonmatchingplatform.lesson_matching_platform.account.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.AuthTokens;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.LoginRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRepository;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtProperties;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    // 로그인 → Access Token + Refresh Token 발급
    public AuthTokens login(LoginRequest request) {
        // Spring Security로 인증 처리 (비밀번호 검증 포함)
        Authentication authentication = authenticationManager.authenticate(                         // DB 유저 정보, 비밀번호 일치 확인
                new UsernamePasswordAuthenticationToken(request.username(), request.password())     // 미인증 Authentication
        );

        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String accessToken = jwtTokenProvider.createAccessToken(username, roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        saveRefreshToken(username, refreshToken);

        return AuthTokens.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // Refresh Token으로 Access Token 재발급 (RTR 방식)
    public AuthTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        String redisKey = REFRESH_TOKEN_PREFIX + username;

        // 서버 Redis에 저장된 Refresh Token과 비교
        Object storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.toString().equals(refreshToken)) {
            redisTemplate.delete(redisKey);
            throw new IllegalStateException("Refresh Token이 일치하지 않거나 만료되었습니다.");
        }

        UserAccount userAccount = userRepository.findByUserId(username)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + username));
        List<String> role = userAccount.getUserRoleSet().stream()
                .map(userRole -> "ROLE_" + userRole.getRole().getRoleType().toString())
                .toList();

        String newAccessToken = jwtTokenProvider.createAccessToken(username, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

        saveRefreshToken(username, newRefreshToken);

        return AuthTokens.of(newAccessToken, newRefreshToken, jwtProperties.getAccessTokenExpiration());
    }

    public AuthTokens issueTokenWithoutPassword(String username) {
        UserAccount userAccount = userRepository.findByUserId(username)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + username));
        List<String> roles = userAccount.getUserRoleSet().stream()
                .map(userRole -> "ROLE_" + userRole.getRole().getRoleType().toString())
                .toList();

        String accessToken = jwtTokenProvider.createAccessToken(username, roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        saveRefreshToken(username, refreshToken);

        return AuthTokens.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // 로그아웃 → Redis에서 Refresh Token 삭제
    public void logout(String username) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    }

    // Redis에 Refresh Token 저장 (Key: "refresh:{username}", TTL: 7일) => 서버 RAM에 저장
    private void saveRefreshToken(String username, String refreshToken) {
        String redisKey = REFRESH_TOKEN_PREFIX + username;

        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }
}
