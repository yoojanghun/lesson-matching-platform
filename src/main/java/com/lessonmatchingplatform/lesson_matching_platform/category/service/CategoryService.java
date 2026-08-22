package com.lessonmatchingplatform.lesson_matching_platform.category.service;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.CategoryCreateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.CategoryUpdateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.response.CategoryWithSubjectResponse;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")                                 // categories 저장소, all이라는 key
    public List<CategoryWithSubjectResponse> getAllCategoriesWithSubject() {
        List<Category> categories = categoryRepository.findAllWithSubjects();

        return categories.stream()
                .map(CategoryWithSubjectResponse::from)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "categories", key = "'all'")
    public Long createCategory(CategoryCreateRequest request) {
        Category newCategory = Category.of(request.name(), request.displayOrder());
        Category category = categoryRepository.save(newCategory);

        return category.getCategoryId();
    }

    @CacheEvict(value = "categories", key = "'all'")
    public void updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리입니다."));

        category.update(request.name(), request.displayOrder());
    }

    // 카테고리 삭제 시 기존 캐시 삭제
    @CacheEvict(value = "categories", key = "'all'")
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("해당 id의 Category가 없습니다."));

        categoryRepository.delete(category);
    }

}
