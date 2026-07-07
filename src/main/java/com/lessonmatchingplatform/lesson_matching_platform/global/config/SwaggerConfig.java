package com.lessonmatchingplatform.lesson_matching_platform.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        // Bearer JWT 인증 스킴 정의
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(BEARER_AUTH);

        // 전체 API에 Bearer 인증 적용
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(BEARER_AUTH);

        return new OpenAPI()
                .info(new Info()
                        .title("Lesson Matching Platform API")
                        .description("레슨 매칭 플랫폼 API 문서 — 로그인 후 발급된 Access Token을 🔒 Authorize 버튼에 입력하세요.")
                        .version("v1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerScheme))
                .addSecurityItem(securityRequirement);
    }
}
