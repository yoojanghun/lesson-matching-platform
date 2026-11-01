package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl.StudentRepositoryCustom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentAccount, Long>, StudentRepositoryCustom {

    @Query("select s from StudentAccount s join fetch s.userAccount where s.studentId = :id")
    Optional<StudentAccount> findProfileById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StudentAccount s WHERE s.studentId = :id")
    Optional<StudentAccount> findByIdWithLock(@Param("id") Long id);
}
