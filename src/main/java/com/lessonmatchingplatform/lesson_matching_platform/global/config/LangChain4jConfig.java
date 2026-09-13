package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ════════════════════════════════════════════════════════════════════
 *  LangChain4jConfig  —  로컬 임베딩 모델 Bean 설정
 *
 *  목적: nGrinder 부하 테스트 시 OpenAI Embedding API(text-embedding-3-small)를
 *        로컬 ONNX 모델(AllMiniLmL6V2)로 교체
 *
 *  이유:
 *   - OpenAI Rate Limit(TPM/RPM)으로 인해 부하 테스트 TPS 측정 불가
 *   - API 호출 비용 발생 없이 순수 서버 성능만 측정 가능
 *   - AllMiniLmL6V2: 384차원 벡터, CPU 완전 지원, 추가 네트워크 없음
 *
 *  @Primary: langchain4j-open-ai-spring-boot-starter 의 자동 구성 Bean을 override
 *
 *  ▶ 운영 전환 시: 이 @Bean을 주석 처리하면 auto-configuration의 OpenAI 모델로 복귀
 * ════════════════════════════════════════════════════════════════════
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    /**
     * 로컬 ONNX 기반 임베딩 모델 Bean.
     *
     * <p>AllMiniLmL6V2EmbeddingModel은 classpath 내 번들된 ONNX 파일을 사용하므로
     * 별도 파일 다운로드나 환경 변수 설정이 불필요합니다.</p>
     *
     * <p>벡터 차원: 384 (기존 OpenAI text-embedding-3-small의 1536차원과 다름)</p>
     *
     * <p><strong>⚠️ 주의:</strong> MongoDB Atlas Vector Search 인덱스의 numDimensions를
     * 1536 → 384 로 변경해야 합니다. 기존 인덱스가 있다면 삭제 후 재생성하세요.</p>
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        log.info("[LangChain4jConfig] ✅ 로컬 AllMiniLmL6V2EmbeddingModel 사용 (벡터 차원: 384)");
        log.info("[LangChain4jConfig] ℹ️  OpenAI Embedding API 호출 없음 — Rate Limit/비용 제로");
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
