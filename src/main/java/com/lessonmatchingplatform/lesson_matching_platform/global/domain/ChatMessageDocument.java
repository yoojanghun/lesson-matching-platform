package com.lessonmatchingplatform.lesson_matching_platform.global.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.type.MessageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")         // MongoDB 컬렉션 이름
@CompoundIndexes({
        @CompoundIndex(name = "idx_matching_created", def = "{'matching_id': 1, 'created_at': -1}"),
        @CompoundIndex(name = "idx_inquiry_created", def = "{'student_id': 1, 'tutor_id': 1, 'matching_id': 1, 'created_at': -1}")
})
public class ChatMessageDocument {

    @Id
    private String id;                          // MongoDB의 ObjectId (자동 생성)

    @Field("matching_id")
    private Long matchingId;

    @Field("student_id")
    private Long studentId;

    @Field("tutor_id")
    private Long tutorId;

    private MessageType type;
    private String sender;
    private String message;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;            // 생성 시간 (채팅 타임라인 정렬용)

    @Builder
    public ChatMessageDocument(Long matchingId, Long studentId, Long tutorId,
                               MessageType type, String sender, String message) {
        this.matchingId = matchingId;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}