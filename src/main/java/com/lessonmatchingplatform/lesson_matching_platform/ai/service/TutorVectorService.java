package com.lessonmatchingplatform.lesson_matching_platform.ai.service;

import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.TutorProfileDto;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.document.Metadata;

@Slf4j
@RequiredArgsConstructor
@Service
public class TutorVectorService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    // 강사 프로필 등록/수정 시 Vector DB에 인덱싱
    public void indexTutorProfile(TutorProfileDto tutorProfile) {
        // 기존 벡터가 있다면 먼저 삭제 후 재등록 (수정 처리)
        deleteTutorProfile(tutorProfile.tutorId());

        // 강사 정보를 의미 단위 텍스트로 변환
        String text = tutorProfile.toPromptText();

        // 메타데이터(강사 ID)를 포함한 TextSegment 생성
        Metadata metadata = Metadata.from("tutorId", String.valueOf(tutorProfile.tutorId()));
        TextSegment segment = TextSegment.from(text, metadata);

        // 텍스트를 임베딩(Vector)으로 변환 후 Vector DB에 저장
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);

        log.info("Tutor Profile Indexed to Vector DB [TutorId: {}]", tutorProfile.tutorId());
    }

    public void deleteTutorProfile(Long tutorId) {
        try {
            Filter filter = new IsEqualTo("tutorId", String.valueOf(tutorId));
            embeddingStore.removeAll(filter);
            log.info("Tutor Profile Deleted from Vector DB [TutorId: {}]", tutorId);
        } catch (Exception e) {
            log.warn("Failed to delete tutor vector or store doesn't support removal by filter. TutorId: {}", tutorId, e);
        }
    }
}
