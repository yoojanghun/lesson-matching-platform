package com.lessonmatchingplatform.lesson_matching_platform.controller;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.StudentSignupRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSignUpRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.StudentMyResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorResponse;
import com.lessonmatchingplatform.lesson_matching_platform.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/sign-up")
@RestController
public class SignUpController {

    private final SignUpService signUpService;

    // Tutor로 처음 sign up 할 때
    @PostMapping("/tutor")
    public TutorResponse signUpTutor(
            @RequestBody TutorSignUpRequest request
    ) {
        return signUpService.signUpTutor(request);
    }

    // Student로 처음 sign up 할 때
    @PostMapping("/student")
    public StudentMyResponse signUpStudent(
            @RequestBody StudentSignupRequest request
    ) {
        return signUpService.signUpStudent(request);
    }

}

// Student와 Tutor로 각각 회원가입 할 때, 일부 공통된 정보는 UserAccount에 저장하도록