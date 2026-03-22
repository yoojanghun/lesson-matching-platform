package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.response.CategoriesAndTutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.service.MainPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping("/pages/home")
    public CategoriesAndTutorsResponse getCategoriesAndTutors() {
        return mainPageService.getCategoriesAndTutors();
    }
}
