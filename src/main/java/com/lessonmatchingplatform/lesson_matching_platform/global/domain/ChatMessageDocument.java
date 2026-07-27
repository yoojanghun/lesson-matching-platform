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

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")         // MongoDB 컬렉션 이름
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