package com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;

import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<UserAccount> findByUserId(String userId);
}
