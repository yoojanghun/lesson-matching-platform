package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.service.TutorsService;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.type.SearchType;
import com.lessonmatchingplatform.lesson_matching_platform.type.SubjectType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/tutors")
@RestController
public class TutorsController {

    @GetMapping
    public List<TutorsResponse> getTutorsList(
            @RequestParam CategoryType category,
            @RequestParam SubjectType subject,
            @RequestParam SearchType searchType
    ) {
        return TutorsService.getTutorsList(category, subject, searchType);
    }
}
