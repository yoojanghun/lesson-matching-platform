package com.lessonmatchingplatform.lesson_matching_platform.global.security.oauth2;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final String userId; // UserAccount's userId
    private final Collection<? extends GrantedAuthority> authorities;           // ? extends는 Read에서만 사용
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    private CustomOAuth2User(String userId, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey) {
        this.userId = userId;
        this.authorities = authorities;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    public static OAuth2User from(UserAccount user, OAuth2Attribute oAuth2Attribute) {
        Collection<GrantedAuthority> authorities = user.getUserRoleSet().stream()
                .<GrantedAuthority>map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleType().name()))
                .toList();

        return new CustomOAuth2User(
                user.getUserId(),
                authorities,
                oAuth2Attribute.attributes(),
                oAuth2Attribute.nameAttributeKey()
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return attributes.get(nameAttributeKey).toString();
    }
}
