package com.lessonmatchingplatform.lesson_matching_platform.global.cache;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청 종료 시 ThreadLocal(CacheContext)을 반드시 초기화하여
 * 톰캣 워커 스레드 재사용으로 인한 캐시 상태 오염을 방지하는 필터
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CacheContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            CacheContext.clear();
        }
    }
}

