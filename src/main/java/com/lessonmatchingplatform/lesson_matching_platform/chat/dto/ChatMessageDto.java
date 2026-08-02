package com.lessonmatchingplatform.lesson_matching_platform.chat.dto;

import com.lessonmatchingplatform.lesson_matching_platform.chat.type.MessageType;
import lombok.Builder;

@Builder
public record ChatMessageDto(
        String mongoId,             // Mongo Object Id
        MessageType type,
        Long matchingId,            // 신청 전 문의는 null, 매칭 진행 중엔 matchingId 값 들어옴
        Long studentId,             // 발신자/수신자 식별용
        Long tutorId,               // 발신자/수신자 식별용
        Long senderId,              // 발신자 Id
        String senderName,          // 발신자 이름/닉네임
        String message,
        boolean isRead              // 읽음 여부
) {

    // senderId, senderName, isRead만 새로운 값으로 바꿈
    public ChatMessageDto withSender(String mongoId, Long senderId, String senderName, boolean isRead) {
        return new ChatMessageDto(
                mongoId,
                this.type,
                this.matchingId,
                this.studentId,
                this.tutorId,
                senderId,
                senderName,
                this.message,
                isRead
        );
    }

    public String getChannelPath() {
        if (this.matchingId != null) {
            return "matching/" + this.matchingId;
        }
        return "inquiry/" + this.studentId + "/" + this.tutorId;
    }
}
