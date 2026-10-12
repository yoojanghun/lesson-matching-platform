package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.data.elasticsearch.uris", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.lessonmatchingplatform.lesson_matching_platform.tutor.search.repository")
public class ElasticsearchConfig {
    // ES URI가 설정된 경우에만 Repository를 활성화합니다.
    // SPRING_ELASTICSEARCH_URIS 환경변수가 없으면 이 Config 자체가 로드되지 않습니다.
}
