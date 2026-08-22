package com.lessonmatchingplatform.lesson_matching_platform.category.service;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.SubjectCreateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.SubjectUpdateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class SubjectService {

    private final CategoryRepository categoryRepository;
    private final SubjectRepository subjectRepository;

    @CacheEvict(value = "categories", key = "'all'")
    public Long createSubject(SubjectCreateRequest request) {
        Long categoryId = request.categoryId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("id에 해당되는 category가 없습니다."));

        Subject newSubject = Subject.of(category, request.name(), request.displayOrder());

        return subjectRepository.save(newSubject).getSubjectId();
    }

    @CacheEvict(value = "categories", key = "'all'")
    public void updateSubject(Long subjectId, SubjectUpdateRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 subject가 없습니다."));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("해당되는 category가 없습니다."));

        subject.update(category, request.name(), request.displayOrder());
    }

    @CacheEvict(value = "categories", key = "'all'")
    public void deleteSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 subject가 없습니다."));

        subjectRepository.delete(subject);
    }
}
