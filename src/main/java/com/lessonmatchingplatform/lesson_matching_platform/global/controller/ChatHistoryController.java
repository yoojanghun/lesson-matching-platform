package com.lessonmatchingplatform.lesson_matching_platform.global.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.global.repository.ChatMessageMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/chat")
@RestController
public class ChatHistoryController {

    private final ChatMessageMongoRepository chatMessageMongoRepository;

    @GetMapping("/history")
    public ResponseEntity<Slice<ChatMessageDocument>> getChatHistory(
            @RequestParam(required = false) Long matchingId,
            @RequestParam Long studentId,
            @RequestParam Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Slice<ChatMessageDocument> history;
        if (matchingId != null) {
            // 매칭 확정건 채팅 내역
            history = chatMessageMongoRepository.findByMatchingIdOrderByCreatedAtDesc(matchingId, pageable);
        } else {
            // 사전 문의 채팅 내역
            history = chatMessageMongoRepository.findByStudentIdAndTutorIdAndMatchingIdIsNullOrderByCreatedAtDesc(
                    studentId, tutorId, pageable
            );
        }

        return ResponseEntity.ok(history);
    }
}
