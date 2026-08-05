package com.lessonmatchingplatform.lesson_matching_platform.category.controller;

import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.CategoryCreateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.CategoryUpdateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.response.CategoryWithSubjectResponse;
import com.lessonmatchingplatform.lesson_matching_platform.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/categories")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    // 전체 카테고리 + 과목 트리 조회 (Redis 캐시 적용)
    @GetMapping
    public ResponseEntity<List<CategoryWithSubjectResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesWithSubject());
    }

    @PostMapping
    public ResponseEntity<Long> createCategory(
            @RequestBody @Valid CategoryCreateRequest request
    ) {
        Long categoryId = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryId);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryUpdateRequest request
    ) {
        categoryService.updateCategory(categoryId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId
    ) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.noContent().build();
    }
}
