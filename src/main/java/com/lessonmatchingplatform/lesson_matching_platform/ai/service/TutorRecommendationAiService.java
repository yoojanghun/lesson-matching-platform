package com.lessonmatchingplatform.lesson_matching_platform.ai.service;

import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorRecommendationResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface TutorRecommendationAiService {

    @SystemMessage("""
            너는 레슨 매칭 플랫폼의 전문 음악 강사 추천 AI 상담원이야.
            제공된 [강사 목록]에서 학생의 [요청 사항]과 가장 잘 맞는 강사 3명을 선정해줘.
            
            각 강사별로 추천 이유(recommendationReason)를 학생의 요청 사항과 연관 지어 2줄 내외로 구체적으로 작성해줘.
            """)
    @UserMessage("""
            [강사 목록]
            {{tutorList}}
            
            [학생 요청 사항]
            {{studentRequirement}}
            """)

    List<TutorRecommendationResponse> recommendTutors(
            @V("tutorList") String tutorList,
            @V("studentRequirement") String studentRequirement
    );
}
