package com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QRole.role;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QUserRole.userRole;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserAccount> findByUserId(String userId) {
        UserAccount content = queryFactory
                .selectFrom(userAccount).distinct()
                .leftJoin(userAccount.userRoleSet, userRole).fetchJoin()
                .leftJoin(userRole.role, role).fetchJoin()
                .where(
                        userAccount.userId.eq(userId)
                )
                .fetchOne();

        return Optional.ofNullable(content);
    }
}
