package com.lessonmatchingplatform.lesson_matching_platform.global.reference.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.StyleTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.LessonGoalRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.LocationRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.TutorStyleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.global.reference.dto.response.ReferenceAllResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReferenceService {

    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final TutorStyleRepository tutorStyleRepository;
    private final LessonGoalRepository lessonGoalRepository;

    public ReferenceAllResponse getAllReferences() {
        List<LocationDto> locations = locationRepository.findAll().stream()
                .map(LocationDto::of)
                .toList();

        List<CategoryTypeDto> categories = categoryRepository.findAll().stream()
                .map(CategoryTypeDto::from)
                .toList();

        List<StyleTypeDto> styles = tutorStyleRepository.findAll().stream()
                .map(StyleTypeDto::of)
                .toList();

        List<GoalTypeDto> goals = lessonGoalRepository.findAll().stream()
                .map(GoalTypeDto::of)
                .toList();

        return ReferenceAllResponse.of(locations, categories, styles, goals);
    }

    public List<LocationDto> getLocations() {
        return locationRepository.findAll().stream()
                .map(LocationDto::of)
                .toList();
    }
}
