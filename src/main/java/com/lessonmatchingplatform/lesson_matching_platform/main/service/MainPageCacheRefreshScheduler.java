package com.lessonmatchingplatform.lesson_matching_platform.main.service;

import com.lessonmatchingplatform.lesson_matching_platform.main.dto.response.MainHomeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 메인 홈 캐시 선갱신 스케줄러
 *
 * [목적] Cache Stampede 완전 방어 (3단계)
 *   - sync=true 방식은 만료 순간 JVM 내 1개 스레드만 DB 진입을 보장하지만,
 *     나머지 스레드는 그 결과를 기다리며 응답 지연이 발생합니다.
 *   - 스케줄러 선갱신은 캐시가 만료되기 전에 미리 갱신하여
 *     사용자 요청 시 항상 캐시 HIT 상태를 유지합니다.
 *
 * [동작 방식]
 *   1. fixedRate 주기(테스트: 5초)마다 DB에서 신선한 데이터를 조회
 *   2. CacheManager.put()으로 Redis 캐시에 직접 저장 (TTL은 RedisConfig에서 관리)
 *   3. 사용자 요청은 항상 캐시를 HIT → DB 조회 0건, concurrent=0
 *
 * [주의사항]
 *   - 스케줄러는 Spring AOP 프록시 외부에서 실행되므로 @Cacheable/@CachePut 사용 불가
 *   - buildMainHomeData()는 @Cacheable 없이 순수 DB 조회만 수행
 *   - 갱신 주기(fixedRate)는 반드시 Redis TTL보다 짧아야 선갱신 효과 발생
 *
 * [현재 설정]
 *   - fixedRate = 5,000ms (5초) ← 테스트용, 운영 기본값: 60,000ms (1분)
 *   - Redis TTL = 15초         ← 테스트용, 운영 기본값: 30분
 *   - 5초 < 15초 조건 충족 → 만료 전 선갱신 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MainPageCacheRefreshScheduler {

    private final MainPageService mainPageService;
    private final CacheManager cacheManager;

    /**
     * 메인 홈 캐시 선갱신
     *
     * fixedRate = 60_000 (1분) ← 운영값 (Redis TTL 30분보다 짧아 만료 전 선갱신 보장)
     * 테스트 시: fixedRate = 5_000 (5초), Redis TTL = 15초
     */
    @Scheduled(fixedRate = 60_000)
    public void refreshMainHomeCache() {
        Cache cache = cacheManager.getCache("mainHome");
        if (cache == null) {
            log.warn("[SCHEDULER:mainHome] CacheManager에서 'mainHome' 캐시를 찾을 수 없음 - 스킵");
            return;
        }

        try {
            long start = System.currentTimeMillis();

            // 스프링 프록시 없이 직접 DB 조회 (트랜잭션 포함, @Cacheable 없음)
            MainHomeResponse freshData = mainPageService.buildMainHomeData(null);

            // CacheManager에 직접 put → Redis에 저장 (TTL은 RedisConfig가 관리)
            cache.put("ALL", freshData);

            log.info("[SCHEDULER:mainHome] 선갱신 완료 | key=ALL | elapsed={}ms",
                    System.currentTimeMillis() - start);

        } catch (Exception e) {
            // 선갱신 실패해도 기존 캐시가 만료되기 전까지는 서비스 정상 유지
            log.warn("[SCHEDULER:mainHome] 선갱신 실패 (기존 캐시 유지) | error={}", e.getMessage());
        }
    }
}

