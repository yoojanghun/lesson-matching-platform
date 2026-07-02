package com.lessonmatchingplatform.lesson_matching_platform.main.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.response.CategoriesAndTutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
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
