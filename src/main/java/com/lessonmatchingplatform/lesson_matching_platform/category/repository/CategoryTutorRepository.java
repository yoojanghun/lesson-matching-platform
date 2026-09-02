package com.lessonmatchingplatform.lesson_matching_platform.category.repository;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.querydsl.CategoryTutorRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryTutorRepository extends JpaRepository<CategoryTutor, Long>, CategoryTutorRepositoryCustom {

    @Query("SELECT ct FROM CategoryTutor ct JOIN FETCH ct.category c WHERE ct.tutorAccount.tutorId IN :tutorIds")
    List<CategoryTutor> findAllByTutorAccount_TutorIdIn(List<Long> tutorIds);
}
