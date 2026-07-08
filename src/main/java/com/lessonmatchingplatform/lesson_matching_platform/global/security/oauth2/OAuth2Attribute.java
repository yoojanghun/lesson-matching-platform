package com.lessonmatchingplatform.lesson_matching_platform.global.security.oauth2;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
public record OAuth2Attribute(
        Map<String, Object> attributes,
        String nameAttributeKey,
        String name,
        String email,
        String provider,
        String providerId
) {
    public static OAuth2Attribute of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("Unsupported provider: " + registrationId);
    }

    private static OAuth2Attribute ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attribute.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
}

// attributes(사용자의 프로필) 형태
//{
//        "sub": "107649283746152439586",        // 구글이 부여한 유저 고유 ID (PK)
//        "name": "유장훈",                       // 유저 이름
//        "given_name": "장훈",
//        "family_name": "유",
//        "picture": "https://lh3.googleusercontent.com/a/AATXAJ...", // 프로필 이미지 URL
//        "email": "janghun@gmail.com",          // 이메일
//        "email_verified": true,
//        "locale": "ko"
//}