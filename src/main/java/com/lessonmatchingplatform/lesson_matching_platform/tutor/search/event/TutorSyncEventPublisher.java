package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.event;

import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.type.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TutorSyncEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;

    public void publishSyncEvent(Long tutorId, EventType eventType) {
        eventPublisher.publishEvent(new TutorSyncEvent(tutorId, eventType));
    }
    
    public void publishSaveEvent(Long tutorId) {
        publishSyncEvent(tutorId, EventType.CREATED_OR_UPDATED);
    }
    
    public void publishDeleteEvent(Long tutorId) {
        publishSyncEvent(tutorId, EventType.DELETED);
    }
}
