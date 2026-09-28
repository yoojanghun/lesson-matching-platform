package com.lessonmatchingplatform.lesson_matching_platform.chat.controller;

import com.lessonmatchingplatform.lesson_matching_platform.chat.domain.ChatMessageDocument;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatMessageDto;
import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.ChatRoomSummaryDto;
import com.lessonmatchingplatform.lesson_matching_platform.chat.repository.ChatMessageMongoRepository;
import com.lessonmatchingplatform.lesson_matching_platform.chat.service.ChatRoomService;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/chat")
@RestController
public class ChatHistoryController {

    private final ChatMessageMongoRepository chatMessageMongoRepository;
    private final ChatRoomService chatRoomService;

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomSummaryDto>> getChatRooms(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        if (boardPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(chatRoomService.getRooms(boardPrincipal.id()));
    }

    @GetMapping("/history")
    public ResponseEntity<Slice<ChatMessageDto>> getChatHistory(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestParam(required = false) Long matchingId,
            @RequestParam Long studentId,
            @RequestParam Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (boardPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long currentUserId = boardPrincipal.id();
        if (!currentUserId.equals(studentId) && !currentUserId.equals(tutorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PageRequest pageable = PageRequest.of(page, size);

        Slice<ChatMessageDocument> history;
        if (matchingId != null) {           // 특정 매칭 건에 국한된 내역 조회 시
            history = chatMessageMongoRepository.findByMatchingIdAndStudentIdAndTutorIdOrderByCreatedAtDesc(
                    matchingId, studentId, tutorId, pageable
            );
        } else {                            // 사전 문의 + 매칭 통합 대화 내역 조회
            history = chatMessageMongoRepository.findByStudentIdAndTutorIdOrderByCreatedAtDesc(
                    studentId, tutorId, pageable
            );
        }

        Slice<ChatMessageDto> historyDto = history.map(ChatMessageDto::fromDocument);

        return ResponseEntity.ok(historyDto);
    }
}
