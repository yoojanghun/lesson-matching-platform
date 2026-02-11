package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<UserAccount> findByUserId(String userId);
}
