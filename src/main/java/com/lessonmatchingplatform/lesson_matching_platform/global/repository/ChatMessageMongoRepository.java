package com.lessonmatchingplatform.lesson_matching_platform.global.repository;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.ChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDocument, String> {

    // 1. 매칭 확정건 채팅 내역 조회 (최신순 페이징)
    Slice<ChatMessageDocument> findByMatchingIdOrderByCreatedAtDesc(Long matchingId, Pageable pageable);

    // 2. 신청 전 사전 문의 채팅 내역 조회 (최신순 페이징)
    Slice<ChatMessageDocument> findByStudentIdAndTutorIdAndMatchingIdIsNullOrderByCreatedAtDesc(
            Long studentId, Long tutorId, Pageable pageable
    );
}
