package com.lessonmatchingplatform.lesson_matching_platform.ai.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Location;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.TutorProfileDto;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.request.TutorRecommendRequest;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorAiMatch;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorRecommendationResponse;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryTutorRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TutorRecommendationServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Mock
    private TutorRecommendationAiService aiService;

    @Mock
    private CategoryTutorRepository categoryTutorRepository;

    @InjectMocks
    private TutorRecommendationService tutorRecommendationService;

    @Test
    @DisplayName("학생 요구사항 텍스트 기반 1차 Vector 필터링 및 LLM 추천 이유 생성 플로우 검증")
    void recommend_success() {
        // given
        String requirement = "취미로 재즈 피아노를 배우고 싶고, 친절한 선생님을 원해요.";
        TutorRecommendRequest request = new TutorRecommendRequest(requirement);

        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        Embedding embedding = Embedding.from(vector);
        given(embeddingModel.embed(requirement)).willReturn(Response.from(embedding));

        TextSegment segment = TextSegment.from("[강사 ID: 1 | 이름: 홍길동] - 대표 프로필 제목: 재즈 피아노 기초부터 차근차근");
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.85, "1", embedding, segment);
        EmbeddingSearchResult<TextSegment> searchResult = new EmbeddingSearchResult<>(List.of(match));

        given(embeddingStore.search(any(EmbeddingSearchRequest.class))).willReturn(searchResult);

        TutorAiMatch aiMatch = new TutorAiMatch(1L, "홍길동", "재즈 피아노 기초 레슨 경험이 풍부하며 친절한 티칭 스타일을 가지고 있어 학생의 요구사항에 잘 부합합니다.");
        given(aiService.recommendTutors(anyString(), anyString())).willReturn(List.of(aiMatch));
        given(categoryTutorRepository.findProfileResponseById(1L)).willReturn(null);

        // when
        List<TutorRecommendationResponse> result = tutorRecommendationService.recommend(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).tutorId()).isEqualTo(1L);
        assertThat(result.get(0).tutorName()).isEqualTo("홍길동");
        assertThat(result.get(0).recommendationReason()).contains("재즈 피아노");

        verify(embeddingModel).embed(requirement);
        verify(embeddingStore).search(any(EmbeddingSearchRequest.class));
        verify(aiService).recommendTutors(anyString(), anyString());
    }
}
