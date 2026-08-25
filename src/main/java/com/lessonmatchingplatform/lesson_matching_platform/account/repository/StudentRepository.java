package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl.StudentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentAccount, Long>, StudentRepositoryCustom {

    @Query("select s from StudentAccount s join fetch s.userAccount where s.studentId = :id")
    Optional<StudentAccount> findByUserAccount_IdWithUserAccount(Long id);
}
