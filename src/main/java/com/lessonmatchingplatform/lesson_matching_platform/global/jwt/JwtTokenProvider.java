package com.lessonmatchingplatform.lesson_matching_platform.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;        // 회원가입한 유저의 정보를 DB에서 찾아주는 조회원(Fetcher)

    // 서명 키 생성
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String createAccessToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles) // JWT Payload에 role 추가
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String username) {
        return buildToken(username, jwtProperties.getRefreshTokenExpiration());
    }

    private String buildToken(String username, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // 토큰에서 username 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰 유효성 검증
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
        String username = claims.getSubject();

        // Claims에서 roles 리스트 추출
        List<?> rawRoles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = Collections.emptyList();
        if (rawRoles != null) {
            authorities = rawRoles.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        // UserDetails 구현체인 Spring Security 기본 User 객체 생성
        UserDetails principal = new User(username, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
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
