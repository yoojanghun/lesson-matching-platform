package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.CategoryWithSubjectResponse;
import com.lessonmatchingplatform.lesson_matching_platform.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")     // 캐시 이름, 그 내부의 캐시 상세 식별자
    public List<CategoryWithSubjectResponse> getAllCategoriesWithSubject() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CategoryWithSubjectResponse::from)
                .toList();
    }

}
