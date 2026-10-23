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
            // 3초 대기, 5초 점유 후 자동 해제
            boolean acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("매칭 락 획득 실패 - lockKey: {}", lockKey);
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
