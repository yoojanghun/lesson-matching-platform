package com.lessonmatchingplatform.lesson_matching_platform.global.security.oauth2;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Role;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.RoleType;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserRole;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.RoleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);                                // 부모 클래스에 정의된 loadUser()사용

        String registrationId = userRequest.getClientRegistration().getRegistrationId();    // 어떤 소셜 기업인가? Google, Kakao 등
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();                          // 구글, 카카오 등의 PK값

        OAuth2Attribute oAuth2Attribute = OAuth2Attribute.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        UserAccount user = saveOrUpdate(oAuth2Attribute);

        return CustomOAuth2User.from(user, oAuth2Attribute);
    }

    private UserAccount saveOrUpdate(OAuth2Attribute attributes) {
        return userRepository.findByEmail(attributes.email())
                .orElseGet(() -> createUser(attributes));
    }

    private UserAccount createUser(OAuth2Attribute attributes) {
        String userId = attributes.provider() + "_" + attributes.providerId();                  // ex.GOOGLE_abcdxxxx
        String tempPassword = UUID.randomUUID().toString();
        String tempPhoneNumber = "oauth2_" + UUID.randomUUID().toString().substring(0, 10);

        Role guestRole = roleRepository.findById(3L)
                .orElseThrow(() -> new IllegalStateException("서버 초기화 에러: GUEST 권한이 DB에 없습니다."));

        UserAccount user = UserAccount.ofOAuth2(
                userId,
                tempPassword,
                attributes.name(),
                attributes.email(),
                attributes.provider(),
                attributes.providerId(),
                tempPhoneNumber,
                guestRole
        );

        return userRepository.save(user);
    }
}
