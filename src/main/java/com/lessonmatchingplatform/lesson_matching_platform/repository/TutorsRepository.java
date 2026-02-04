package com.lessonmatchingplatform.lesson_matching_platform.repository;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl.TutorsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorsRepository extends JpaRepository<TutorAccount, Long>, TutorsRepositoryCustom {
}
