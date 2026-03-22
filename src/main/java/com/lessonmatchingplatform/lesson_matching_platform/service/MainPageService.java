package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.CategoriesAndTutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.repository.TutorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class MainPageService {

    private final CategoryRepository categoryRepository;
    private final TutorsRepository tutorsRepository;

    @Transactional(readOnly = true)
    public CategoriesAndTutorsResponse getCategoriesAndTutors() {
        List<Category> categories = categoryRepository.findAll();
        Map<Long, List<TutorAccount>> tutors = categories.stream()
                .collect(Collectors.toMap(
                        Category::getCategoryId,
                        category -> tutorsRepository.searchPopularTutors(category.getCategoryId())
                ));
        return CategoriesAndTutorsResponse.of(categories, tutors);
    }
}
