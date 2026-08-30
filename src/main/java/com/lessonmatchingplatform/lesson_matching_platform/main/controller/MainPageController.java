package com.lessonmatchingplatform.lesson_matching_platform.main.controller;

import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.response.MainHomeResponse;
import com.lessonmatchingplatform.lesson_matching_platform.main.service.MainPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/main")
@RestController
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping("/home")
    public ResponseEntity<MainHomeResponse> getPopularTutorsList(
            @RequestParam(defaultValue = "1") Long categoryId
    ) {
        return ResponseEntity.ok(mainPageService.getMainHomeData(categoryId));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<TutorCardDto>> getTrendingTutors(
            @RequestParam Long categoryId
    ) {
        List<TutorCardDto> trendingTutors = mainPageService.getTrendingTutorsByCategory(categoryId);
        return ResponseEntity.ok(trendingTutors);
    }

    @GetMapping("rookie")
    public ResponseEntity<List<TutorCardDto>> getRookieTutors(
            @RequestParam Long categoryId
    ) {
        List<TutorCardDto> rookieTutors = mainPageService.getRookieTutorsByCategory(categoryId);
        return ResponseEntity.ok(rookieTutors);
    }

}
