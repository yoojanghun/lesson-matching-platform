package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.service.TutorSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ════════════════════════════════════════════════════════════════════
 *  TutorEsDataInitializer
 *
 *  앱 기동 직후 MySQL의 profileStatus = COMPLETED 튜터 데이터를
 *  Elasticsearch(tutors 인덱스)에 자동으로 동기화합니다.
 *
 *  동작 시점: Spring Context 완전 로딩 후 (data.sql 실행 이후)
 *  대상 데이터: TutorAccount.profileStatus == COMPLETED 인 모든 튜터
 *
 *  ▶ nGrinder 부하 테스트 전 별도 작업 없이 ES 데이터가 자동 적재됩니다.
 *  ▶ 운영 환경에서는 이 클래스를 제거하거나 @Profile("local")로 제한하세요.
 * ════════════════════════════════════════════════════════════════════
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TutorEsDataInitializer implements ApplicationRunner {

    private final TutorSyncService tutorSyncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[TutorEsDataInitializer] MySQL → Elasticsearch 튜터 데이터 동기화 시작...");

        try {
            int synced = tutorSyncService.syncAllCompletedTutors();
            log.info("[TutorEsDataInitializer] 동기화 완료 — {}명의 튜터가 ES에 색인되었습니다.", synced);
        } catch (Exception e) {
            // ES가 아직 준비되지 않은 경우 등 비치명적 오류로 처리 (앱 기동 실패 방지)
            log.warn("[TutorEsDataInitializer] ES 동기화 중 오류 발생 (앱 기동은 계속 진행): {}", e.getMessage());
        }
    }
}
