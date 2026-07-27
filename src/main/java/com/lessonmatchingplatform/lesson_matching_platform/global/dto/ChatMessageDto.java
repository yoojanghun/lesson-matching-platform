package com.lessonmatchingplatform.lesson_matching_platform.global.dto;

import com.lessonmatchingplatform.lesson_matching_platform.global.type.MessageType;

public record ChatMessageDto(
        MessageType type,
        Long matchingId,
        String sender,           // 보낸 사람 (유저 이메일/아이디)
        String message
) {

    public ChatMessageDto withSender(String authenticatedSender) {
        return new ChatMessageDto(
                this.type,
                this.matchingId,
                authenticatedSender,
                this.message
        );
    }
}
