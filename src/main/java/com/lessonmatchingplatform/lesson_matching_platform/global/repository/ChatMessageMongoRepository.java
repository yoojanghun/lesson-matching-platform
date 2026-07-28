package com.lessonmatchingplatform.lesson_matching_platform.global.repository;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.ChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDocument, String> {

    // 1. 매칭 확정건 채팅 내역 조회 (최신순 페이징)
    Slice<ChatMessageDocument> findByMatchingIdOrderByCreatedAtDesc(Long matchingId, Pageable pageable);

    // 2. 신청 전 사전 문의 채팅 내역 조회 (최신순 페이징)
    Slice<ChatMessageDocument> findByStudentIdAndTutorIdAndMatchingIdIsNullOrderByCreatedAtDesc(
            Long studentId, Long tutorId, Pageable pageable);

    // 매칭 채팅방 읽음 처리 (상대방이 보낸 unread 메시지만 targets)
    @Query("{ 'matching_id': ?0, 'sender_id': { $ne: ?1 }, 'is_read': false }")
    List<ChatMessageDocument> findUnreadMatchingMessages(Long matchingId, Long currentUserId);

    // 사전 문의 채팅방 읽음 처리
    @Query("{ 'student_id': ?0, 'tutor_id': ?1, 'matching_id': null, 'sender_id': { $ne: ?2 }, 'is_read': false }")
    List<ChatMessageDocument> findUnreadInquiryMessages(Long studentId, Long tutorId, Long currentUserId);
}
