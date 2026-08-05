package com.lessonmatchingplatform.lesson_matching_platform.ai.service;

import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.request.TutorRecommendRequest;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorRecommendationResponse;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class TutorRecommendationService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final TutorRecommendationAiService aiService;

    public List<TutorRecommendationResponse> recommend(TutorRecommendRequest request) {
        String studentRequirement = request.studentRequirement();

        // 학생 요구사항 텍스트를 Vector로 변환
        Embedding queryEmbedding = embeddingModel.embed(studentRequirement).content();

        // Vector DB에서 의미적 유사도가 가장 높은 상위 10명의 강사만 Retrieval (1차 필터링)
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(10)
                .minScore(0.6)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        // 10명의 강사 프로필 텍스트를 하나의 Prompt 문자열로 결합
        String candidateTutorsText = searchResult.matches().stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n---\n"));

        if (candidateTutorsText.isBlank()) {
            log.warn("Vector DB에서 조건에 맞는 강사를 찾지 못했습니다. requirement: {}", studentRequirement);
            return List.of();
        }

        return aiService.recommendTutors(candidateTutorsText, studentRequirement);
    }
}
