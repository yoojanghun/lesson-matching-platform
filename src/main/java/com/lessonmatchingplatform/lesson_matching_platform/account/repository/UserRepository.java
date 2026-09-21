package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long>, UserRepositoryCustom {
    Optional<UserAccount> findByEmail(String email);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.userRoleSet ur JOIN FETCH ur.role WHERE u.userId = :userId")
    Optional<UserAccount> findByUserIdWithRoles(String userId);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.userRoleSet ur JOIN FETCH ur.role WHERE u.id = :id")
    Optional<UserAccount> findByIdWithRoles(Long id);
}
