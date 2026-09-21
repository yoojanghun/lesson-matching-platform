package com.lessonmatchingplatform.lesson_matching_platform.global.jwt;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     * @param activeRole 현재 활성 역할 (예: "ROLE_STUDENT"). 보유 역할 중 하나여야 함.
     */
    public String createAccessToken(Long userId, String username, List<String> roles, String activeRole) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)           // 보유 역할 전체 목록
                .claim("activeRole", activeRole)  // 현재 활성 역할
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Refresh Token 생성 — activeRole 클레임 포함
     * Access Token 만료 후 재발급 시 역할 전환 상태를 유지하기 위해 activeRole을 Refresh Token에도 기록합니다.
     */
    public String createRefreshToken(String username, String activeRole) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("activeRole", activeRole)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰에서 현재 활성 역할 클레임 추출 (예: "ROLE_STUDENT"). Access Token / Refresh Token 모두 사용 가능.
    public String getActiveRole(String token) {
        return parseClaims(token).get("activeRole", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 토큰: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT 서명 오류: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims가 비어있음: {}", e.getMessage());
        }
        return false;
    }

    // 토큰으로 Authentication(인증 토큰 신분증) 객체 반환
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        List<?> rawRoles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = Collections.emptyList();
        if (rawRoles != null) {
            authorities = rawRoles.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        BoardPrincipal principal = BoardPrincipal.of(userId, username, authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    // 토큰 비밀 키로 검증한 후, payload 반환
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())        // Jwt parser는 sign key로 검증하도록 설정
                .build()                            // Jwt parser 빌드
                .parseSignedClaims(token)           // 그 parser가 token 검증
                .getPayload();                      // token의 payload 반환
    }
}
