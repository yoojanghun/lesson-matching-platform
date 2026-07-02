package com.lessonmatchingplatform.lesson_matching_platform.category.repository;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
