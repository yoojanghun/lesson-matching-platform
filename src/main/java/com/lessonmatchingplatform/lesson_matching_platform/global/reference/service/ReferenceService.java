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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.type.TutorSortType;
import com.lessonmatchingplatform.lesson_matching_platform.global.reference.dto.response.EnumReferenceDto;
import java.util.Arrays;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReferenceService {

    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final TutorStyleRepository tutorStyleRepository;
    private final LessonGoalRepository lessonGoalRepository;
    
    @Cacheable(value = "references")
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

        List<EnumReferenceDto> lessonTypes = Arrays.stream(LessonType.values())
                .map(type -> new EnumReferenceDto(type.name(), type.getDescription()))
                .toList();

        List<EnumReferenceDto> sortTypes = Arrays.stream(TutorSortType.values())
                .map(type -> new EnumReferenceDto(type.name(), type.getDescription()))
                .toList();

        return ReferenceAllResponse.of(locations, categories, styles, goals, lessonTypes, sortTypes);
    }

    @Cacheable(value = "locations")
    public List<LocationDto> getLocations() {
        return locationRepository.findAll().stream()
                .map(LocationDto::of)
                .toList();
    }
}
