package com.lessonmatchingplatform.lesson_matching_platform.account.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.AuthTokens;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.LoginRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRepository;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtProperties;
import com.lessonmatchingplatform.lesson_matching_platform.global.jwt.JwtTokenProvider;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Transactional
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

        BoardPrincipal principal = (BoardPrincipal) authentication.getPrincipal();
        String username = principal.getUsername();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String activeRole = resolveActiveRole(roles);

        String accessToken  = jwtTokenProvider.createAccessToken(principal.id(), username, roles, activeRole);
        String refreshToken = jwtTokenProvider.createRefreshToken(username, activeRole);

        saveRefreshToken(username, refreshToken);
        return AuthTokens.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Refresh Token으로 Access Token 재발급 (RTR 방식)
     *
     * activeRole 유지 전략:
     *  1) Refresh Token 클레임에서 activeRole 추출
     *  2) DB에서 현재 유저 권한 조회 후 해당 역할이 아직 유효한지 검증
     *  3) 유효하지 않으면 resolveActiveRole()로 fallback
     * -> Access Token 만료 후에도 역할 전환 상태가 유지됨
     */
    public AuthTokens refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        String redisKey  = REFRESH_TOKEN_PREFIX + username;

        // Redis 저장 토큰과 비교 (RTR)
        Object storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.toString().equals(refreshToken)) {
            redisTemplate.delete(redisKey);
            throw new IllegalStateException("Refresh Token이 일치하지 않거나 만료되었습니다.");
        }

        // DB에서 Fetch Join으로 역할 조회 (N+1 방지)
        UserAccount userAccount = userRepository.findByUserIdWithRoles(username)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + username));

        List<String> roles = userAccount.getUserRoleSet().stream()
                .map(userRole -> "ROLE_" + userRole.getRole().getRoleType().toString())
                .toList();

        // Refresh Token에서 activeRole 추출 후 유효성 검증
        String activeRoleFromToken = jwtTokenProvider.getActiveRole(refreshToken);
        String activeRole;
        if (activeRoleFromToken != null && roles.contains(activeRoleFromToken)) {
            activeRole = activeRoleFromToken;       // 역할 전환 상태 유지
        } else {
            activeRole = resolveActiveRole(roles);  // 권한 변경 등 예외 상황 fallback
        }

        String newAccessToken  = jwtTokenProvider.createAccessToken(userAccount.getId(), username, roles, activeRole);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username, activeRole);

        saveRefreshToken(username, newRefreshToken);
        return AuthTokens.of(newAccessToken, newRefreshToken, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * 역할 전환 — 보유 역할 중 targetRole로 activeRole을 바꾼 새 토큰을 발급합니다.
     */
    public AuthTokens switchRole(Long userId, String targetRole) {
        UserAccount userAccount = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + userId));

        String username = userAccount.getUserId();

        List<String> roles = userAccount.getUserRoleSet().stream()
                .map(userRole -> "ROLE_" + userRole.getRole().getRoleType().toString())
                .toList();

        String normalizedTarget = "ROLE_" + targetRole.toUpperCase().replace("ROLE_", "");
        if (roles.stream().noneMatch(r -> r.equalsIgnoreCase(normalizedTarget))) {
            throw new AccessDeniedException("해당 계정은 " + targetRole + " 역할을 보유하고 있지 않습니다.");
        }

        String accessToken  = jwtTokenProvider.createAccessToken(userAccount.getId(), username, roles, normalizedTarget);
        String refreshToken = jwtTokenProvider.createRefreshToken(username, normalizedTarget);

        saveRefreshToken(username, refreshToken);
        return AuthTokens.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // 비밀번호 없이 토큰 발급 (OAuth2 역할 확정, 소셜 가입 완료 등)
    public AuthTokens issueTokenWithoutPassword(Long id) {
        UserAccount userAccount = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + id));

        String username = userAccount.getUserId();

        List<String> roles = userAccount.getUserRoleSet().stream()
                .map(userRole -> "ROLE_" + userRole.getRole().getRoleType().toString())
                .toList();

        String activeRole = resolveActiveRole(roles);
        String accessToken  = jwtTokenProvider.createAccessToken(userAccount.getId(), username, roles, activeRole);
        String refreshToken = jwtTokenProvider.createRefreshToken(username, activeRole);

        saveRefreshToken(username, refreshToken);
        return AuthTokens.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    public void logout(String username) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    }

    private void saveRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + username,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 보유 역할 목록 중 기본 활성 역할을 결정합니다.
     * GUEST가 아닌 역할 중 첫 번째를 선택하고, 모두 GUEST면 ROLE_GUEST를 반환합니다.
     */
    private String resolveActiveRole(List<String> roles) {
        return roles.stream()
                .filter(role -> !role.equalsIgnoreCase("ROLE_GUEST"))
                .findFirst()
                .orElseGet(() -> roles.isEmpty() ? "ROLE_GUEST" : roles.getFirst());
    }
}