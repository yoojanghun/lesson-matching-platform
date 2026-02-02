package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.type.SearchType;
import com.lessonmatchingplatform.lesson_matching_platform.type.SubjectType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TutorsService {
    public static List<TutorsResponse> getTutorsList(CategoryType category, SubjectType subject, SearchType searchType) {

    }
}
