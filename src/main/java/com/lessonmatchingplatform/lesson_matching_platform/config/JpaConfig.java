package com.lessonmatchingplatform.lesson_matching_platform.config;

import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(BoardPrincipal.class::isInstance)
                .map(BoardPrincipal.class::cast)
                .map(BoardPrincipal::getUsername)
                .or(() -> Optional.of("system"));
    }
}

// SecurityContextHolder.getContext(): 현재 스레드에 저장된 보안 컨텍스트(로그인 정보)를 가져옴.
// filter(Authentication::isAuthenticated): 현재 사용자가 실제로 인증(로그인)된 상태인지 확인.
// filter(BoardPrincipal.class::isInstance): 로그인한 정보(Principal)가 정의한 BoardPrincipal 클래스 타입인지 확인.
// map(BoardPrincipal::getUsername): BoardPrincipal 객체에서 유저네임(아이디)을 꺼냄.
// or(() -> Optional.of("system")): 만약 로그인을 안 했거나(예: 회원가입 시점), 인증 정보가 없다면 기본값으로 "system"을 저장.