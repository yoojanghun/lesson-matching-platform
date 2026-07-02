package com.lessonmatchingplatform.lesson_matching_platform.tutor.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.querydsl.TutorsRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorsRepository extends JpaRepository<TutorAccount, Long>, TutorsRepositoryCustom {
}
