package com.lessonmatchingplatform.lesson_matching_platform.chat.controller;

import com.lessonmatchingplatform.lesson_matching_platform.chat.dto.request.ChatReadRequest;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.chat.service.ChatReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/chat")
@RestController
public class ChatReadController {

    private final ChatReadService chatReadService;

    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody ChatReadRequest request
    ) {
        if (boardPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long currentUserId = boardPrincipal.id();
        if (request.studentId() != null && request.tutorId() != null &&
                !currentUserId.equals(request.studentId()) && !currentUserId.equals(request.tutorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        chatReadService.markMessagesAsRead(request, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveRoom(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody ChatReadRequest request
    ) {
        if (boardPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long currentUserId = boardPrincipal.id();
        if (request.studentId() != null && request.tutorId() != null &&
                !currentUserId.equals(request.studentId()) && !currentUserId.equals(request.tutorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        chatReadService.leaveRoom(request, currentUserId);
        return ResponseEntity.ok().build();
    }
}
