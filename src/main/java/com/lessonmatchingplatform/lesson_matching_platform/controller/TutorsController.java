package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSwitchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.service.TutorsService;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/tutors")
@RequiredArgsConstructor
@RestController
public class TutorsController {

    private final TutorsService tutorsService;

    @GetMapping
    public Page<TutorsResponse> getTutorsList(
            TutorSearchCondition tutorSearchCondition,
            Pageable pageable
    ) {
        return tutorsService.getTutorsList(tutorSearchCondition, pageable);
    }

    @GetMapping("/{tutorId}")
    public TutorResponse getTutor(
            @PathVariable Long tutorId
    ) {
        return tutorsService.getTutor(tutorId);
    }


}
