package com.lessonmatchingplatform.lesson_matching_platform.ai.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.request.TutorRecommendRequest;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorAiMatch;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorRecommendationResponse;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryTutorRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TutorRecommendationService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final TutorRecommendationAiService aiService;
    private final CategoryTutorRepository categoryTutorRepository;

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

        // 3. LLM 추천 사유 및 튜터 목록 추출 (2차 RAG)
        List<TutorAiMatch> aiMatches = aiService.recommendTutors(candidateTutorsText, studentRequirement);

        if (aiMatches.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. [N+1 해결] AI가 추천한 tutorId 목록만 추출
        List<Long> tutorIds = aiMatches.stream()
                .map(TutorAiMatch::tutorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 5. [단 1번의 묶음 쿼리] IN 쿼리로 모든 프로필 조회 후 Map 변환
        List<TutorProfileResponse> profiles = categoryTutorRepository.findProfileResponseByIds(tutorIds);
        Map<Long, TutorProfileResponse> profileMap = profiles.stream()
                .collect(Collectors.toMap(TutorProfileResponse::tutorId, Function.identity(), (p1, p2) -> p1));

        // 6. AI 추천 순서를 유지하며 프로필을 조립
        return aiMatches.stream()
                .filter(match -> match.tutorId() != null)
                .map(match -> {
                    TutorProfileResponse profile = profileMap.get(match.tutorId());
                    if (profile == null) {
                        log.warn("프로필을 찾을 수 없는 tutorId: {}", match.tutorId());
                    }
                    return TutorRecommendationResponse.of(
                            match.tutorId(),
                            match.tutorName(),
                            match.recommendationReason(),
                            profile
                    );
                })
                .toList();
    }
}
