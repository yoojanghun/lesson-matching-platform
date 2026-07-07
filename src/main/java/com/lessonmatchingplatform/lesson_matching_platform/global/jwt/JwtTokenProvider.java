package com.lessonmatchingplatform.lesson_matching_platform.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

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
    public String createAccessToken(String username) {
        return buildToken(username, jwtProperties.getAccessTokenExpiration());
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
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);      // UserDetails: 전용 회원 정보 규격 (비밀번호, 권한, 계정 만료 여부)
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
