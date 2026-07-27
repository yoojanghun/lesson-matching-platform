package com.lessonmatchingplatform.lesson_matching_platform.global.dto;

import com.lessonmatchingplatform.lesson_matching_platform.global.type.MessageType;

public record ChatMessageDto(
        MessageType type,
        Long matchingId,            // 신청 전 문의는 null, 매칭 진행 중엔 matchingId 값 들어옴
        Long studentId,             // 발신자/수신자 식별용
        Long tutorId,               // 발신자/수신자 식별용
        String sender,
        String message
) {

    public ChatMessageDto withSender(String authenticatedSender) {
        return new ChatMessageDto(
                this.type,
                this.matchingId,
                this.studentId,
                this.tutorId,
                authenticatedSender,
                this.message
        );
    }

    public String getChannelPath() {
        if (this.matchingId != null) {
            return "matching/" + this.matchingId;
        }
        return "inquiry/" + this.studentId + "/" + this.tutorId;
    }
}
