package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.lessonmatchingplatform.lesson_matching_platform.tutor.search.repository")
public class ElasticsearchConfig {
    // Spring Boot Auto-configuration will handle the client setup based on application.yml properties.
    // This class primarily enables Elasticsearch repositories for specific packages.
}
