package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.TutorProfileDto;
import com.lessonmatchingplatform.lesson_matching_platform.ai.service.TutorVectorService;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class TutorVectorDataInitializer implements ApplicationRunner {
    private final TutorsRepository tutorsRepository;
    private final TutorVectorService tutorVectorService;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        log.info("[TutorVectorDataInitializer] MySQL → Vector DB 튜터 재색인 시작...");
        try {
            List<TutorAccount> completedTutors =
                    tutorsRepository.findAllByProfileStatus(ProfileStatus.COMPLETED);
            List<TutorProfileDto> dtos = completedTutors.stream()
                    .map(TutorProfileDto::from)
                    .toList();
            int indexed = tutorVectorService.reindexAll(dtos);
            log.info("[TutorVectorDataInitializer] 재색인 완료 — {}명 색인됨", indexed);
        } catch (Exception e) {
            log.warn("[TutorVectorDataInitializer] Vector DB 재색인 중 오류 (앱 기동 계속 진행): {}", e.getMessage());
        }
    }
}