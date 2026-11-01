package com.lessonmatchingplatform.lesson_matching_platform.lesson.facade;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.service.LessonMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingLockFacade {

    private final RedissonClient redissonClient;
    private final LessonMatchingService lessonMatchingService;

    public Long lessonMatchingWithLock(Long studentId, Long tutorId, LessonMatchingRequest request) {
        String lockKey = "lock:matching:" + studentId + ":" + tutorId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // waitTime 0초: 락을 선점한 1개 요청 외 나머지 요청은 Redis 메모리 수준에서 즉시 튕겨냄 (Fast-Fail)
            // leaseTime 5초: 작업 처리 중 장애 발생 시 5초 후 자동 해제
            boolean acquired = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("매칭 락 획득 실패 (Fast-Fail) - lockKey: {}", lockKey);
                throw new IllegalStateException("현재 매칭 요청을 처리 중입니다. 잠시 후 다시 시도해 주세요.");
            }

            // DB 트랜잭션 실행 (커밋 완료 후 finally에서 락 해제 보장)
            return lessonMatchingService.lessonMatching(studentId, tutorId, request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("매칭 처리 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
