package com.lessonmatchingplatform.lesson_matching_platform.service.security;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BoardUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username)  {        // username은 로그인한 아이디
        UserAccount userAccount = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다." + username));

        return BoardPrincipal.from(userAccount);
    }
}

// 사용자 요청: 사용자가 로그인 폼에서 아이디(username)와 비번(password)을 입력하고 제출(POST /login)
// Filter가 가로채기: UsernamePasswordAuthenticationFilter이 요청을 가로챔
// 관리자에게 전달: 필터는 아이디와 비번을 들고 AuthenticationManager라는 관리자에게 "이 사람 맞는 사람인지 확인해줘"라고 부탁합니다.
// UserDetailsService 호출: 관리자는 등록된 UserDetailsService를 찾아서 loadUserByUsername(입력한_아이디)를 실행합니다.
// DB 조회: 우리가 만든 로직에 따라 DB에서 사용자를 찾고 BoardPrincipal을 반환합니다.
// 비밀번호 검증: Security가 DB에서 가져온 비밀번호와 사용자가 입력한 비밀번호가 일치하는지 자동으로 비교합니다. (이때 PasswordEncoder가 쓰입니다.)