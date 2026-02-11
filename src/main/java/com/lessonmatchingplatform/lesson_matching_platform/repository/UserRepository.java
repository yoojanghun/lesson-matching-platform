package com.lessonmatchingplatform.lesson_matching_platform.repository;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long>, UserRepositoryCustom {
}
