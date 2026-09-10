package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.event;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.TutorProfileDto;
import com.lessonmatchingplatform.lesson_matching_platform.ai.service.TutorVectorService;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document.TutorDocument;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.repository.TutorSearchRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.service.TutorSyncService;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.type.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TutorSyncEventListener {

    private final TutorsRepository tutorsRepository;
    private final TutorSearchRepository tutorSearchRepository;
    private final TutorSyncService tutorSyncService;
    private final TutorVectorService tutorVectorService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTutorSyncEvent(TutorSyncEvent event) {
        log.info("Handling TutorSyncEvent for tutorId: {}, eventType: {}", event.tutorId(), event.eventType());

        if (event.eventType() == EventType.DELETED) {
            tutorSearchRepository.deleteById(event.tutorId());
            tutorVectorService.deleteTutorProfile(event.tutorId());
            log.info("Successfully deleted TutorDocument and Vector for tutorId: {}", event.tutorId());
            return;
        }

        tutorsRepository.findById(event.tutorId()).ifPresent(tutorAccount -> {
            if (tutorAccount.getProfileStatus() != ProfileStatus.COMPLETED) {
                tutorSearchRepository.deleteById(event.tutorId());
                tutorVectorService.deleteTutorProfile(event.tutorId());
                log.info("TutorAccount profile is incomplete. Deleted/skipped from Elasticsearch and Vector DB. tutorId: {}", event.tutorId());
                return;
            }

            TutorDocument document = tutorSyncService.toDocument(tutorAccount);
            tutorSearchRepository.save(document);

            TutorProfileDto profileDto = TutorProfileDto.from(tutorAccount);
            tutorVectorService.indexTutorProfile(profileDto);

            log.info("Successfully synced TutorAccount to Elasticsearch and Vector DB. tutorId: {}", event.tutorId());
        });
    }
}
