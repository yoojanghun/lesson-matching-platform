package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.event;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
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

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTutorSyncEvent(TutorSyncEvent event) {
        log.info("Handling TutorSyncEvent for tutorId: {}, eventType: {}", event.tutorId(), event.eventType());

        if (event.eventType() == EventType.DELETED) {
            tutorSearchRepository.deleteById(event.tutorId());
            log.info("Successfully deleted TutorDocument from Elasticsearch. tutorId: {}", event.tutorId());
            return;
        }

        tutorsRepository.findById(event.tutorId()).ifPresent(tutorAccount -> {
            if (tutorAccount.getProfileStatus() != ProfileStatus.COMPLETED) {
                tutorSearchRepository.deleteById(event.tutorId());
                log.info("TutorAccount profile is incomplete. Deleted/skipped from Elasticsearch. tutorId: {}", event.tutorId());
                return;
            }

            TutorDocument document = tutorSyncService.toDocument(tutorAccount);
            tutorSearchRepository.save(document);
            log.info("Successfully synced TutorAccount to Elasticsearch. tutorId: {}", event.tutorId());
        });
    }
}
