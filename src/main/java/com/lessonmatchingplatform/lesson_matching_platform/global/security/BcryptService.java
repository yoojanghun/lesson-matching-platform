package com.lessonmatchingplatform.lesson_matching_platform.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

/**
 * Bcrypt 연산을 전용 스레드 풀에서 실행하는 서비스 (Bulkhead 패턴)
 *
 * @Async("bcryptExecutor") 를 통해 BcryptConfig에서 정의한 전용 풀에서 실행됨.
 * Tomcat 스레드는 이 메서드를 호출하고 CompletableFuture로 완료를 대기하므로
 * Bcrypt 실행 중에도 다른 요청을 처리할 수 있음.
 *
 * 풀이 가득 찰 경우 RejectedExecutionException 발생 →
 * GlobalExceptionHandler에서 잡아 429 Too Many Requests 반환.
 */
@Service
@RequiredArgsConstructor
public class BcryptService {

    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 해싱 (Bcrypt 전용 스레드 풀에서 비동기 실행)
     *
     * @param rawPassword 평문 비밀번호
     * @return 해싱된 비밀번호를 담은 CompletableFuture
     * @throws RejectedExecutionException 전용 풀 큐가 가득 찬 경우 (429 반환 트리거)
     */
    @Async("bcryptExecutor")
    public CompletableFuture<String> encode(String rawPassword) {
        return CompletableFuture.completedFuture(passwordEncoder.encode(rawPassword));
    }

    /**
     * 비밀번호 검증 (Bcrypt 전용 스레드 풀에서 비동기 실행)
     *
     * @param rawPassword     평문 비밀번호
     * @param encodedPassword 저장된 해싱 비밀번호
     * @return 일치 여부를 담은 CompletableFuture
     * @throws RejectedExecutionException 전용 풀 큐가 가득 찬 경우 (429 반환 트리거)
     */
    @Async("bcryptExecutor")
    public CompletableFuture<Boolean> matches(String rawPassword, String encodedPassword) {
        return CompletableFuture.completedFuture(passwordEncoder.matches(rawPassword, encodedPassword));
    }
}
