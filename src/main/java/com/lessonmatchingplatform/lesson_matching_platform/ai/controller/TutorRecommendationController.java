package com.lessonmatchingplatform.lesson_matching_platform.ai.controller;

import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.request.TutorRecommendRequest;
import com.lessonmatchingplatform.lesson_matching_platform.ai.dto.response.TutorRecommendationResponse;
import com.lessonmatchingplatform.lesson_matching_platform.ai.service.TutorRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/ai")
@RestController
public class TutorRecommendationController {

    private final TutorRecommendationService tutorRecommendationService;

    @PostMapping("/recommend-tutors")
    public ResponseEntity<List<TutorRecommendationResponse>> recommendTutors(
            @RequestBody TutorRecommendRequest request
    ) {
        List<TutorRecommendationResponse> result = tutorRecommendationService.recommend(request);

        return ResponseEntity.ok(result);
    }
}
