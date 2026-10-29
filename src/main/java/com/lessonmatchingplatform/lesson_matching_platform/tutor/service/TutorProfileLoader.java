package com.lessonmatchingplatform.lesson_matching_platform.tutor.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 캐시 미스 시 실제 DB 조회를 담당하는 로더 컴포넌트
 *
 * TutorsService와 분리하여 스프링 프록시 기반의 @Transactional(readOnly = true)가
 * 정상적으로 적용되도록 보장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TutorProfileLoader {

    private final TutorsRepository tutorsRepository;
    private final CategoryTutorRepository categoryTutorRepository;

    private final AtomicInteger dbQueryCount = new AtomicInteger();

    @Transactional(readOnly = true)
    public TutorProfileResponse loadProfile(Long tutorId) {
        log.warn("🔥 [CACHE MISS][redisson-lock] DB 직접 조회 발생! tutorId={} | thread={} | 누적DB조회수={}",
                tutorId, Thread.currentThread().getName(), dbQueryCount.incrementAndGet());

        try {
            // DB 쿼리/연산 지연 재현 (Cache Stampede 테스트 동안 만료 시 스레드 경합을 관찰하기 위한 장치: sleep 150ms 조건)
            Thread.sleep(150);
        } catch (InterruptedException ignored) {}

        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        return categoryTutorRepository.findProfileResponseById(tutorId);
    }
}

