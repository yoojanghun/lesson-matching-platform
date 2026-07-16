package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteByUserAccount(UserAccount userAccount);
}
