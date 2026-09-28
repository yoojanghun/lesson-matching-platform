package com.lessonmatchingplatform.lesson_matching_platform.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomSummaryDto(
        String roomId,
        Long matchingId,
        Long studentId,
        Long tutorId,
        Long partnerId,
        String partnerName,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount
) {
}