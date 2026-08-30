package com.lessonmatchingplatform.lesson_matching_platform.account.controller;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentProfilePatchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentProfileRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorProfilePatchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorProfileRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.StudentProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.account.service.ProfileService;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/profile")
@RestController
public class ProfileController {

    private final ProfileService profileService;

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/me")
    public ResponseEntity<StudentProfileResponse> getMyStudentProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        Long id = boardPrincipal.id();
        StudentProfileResponse studentProfileResponse = profileService.getMyStudentProfile(id);

        return ResponseEntity.ok(studentProfileResponse);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/student/me")
    public ResponseEntity<Void> postMyStudentProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid StudentProfileRequest request
    ) {
        Long id = boardPrincipal.id();
        profileService.postMyStudentProfile(id, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/student/me")
    public ResponseEntity<Void> putMyStudentProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid StudentProfileRequest request
    ) {
        Long id = boardPrincipal.id();
        profileService.putMyStudentProfile(id, request);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PatchMapping("/student/me")
    public ResponseEntity<Void> patchMyStudentProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid StudentProfilePatchRequest request
    ) {
        Long id = boardPrincipal.id();
        profileService.patchMyStudentProfile(id, request);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/tutor/me")
    public ResponseEntity<TutorProfileResponse> getMyTutorProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        Long id = boardPrincipal.id();
        TutorProfileResponse tutorProfileResponse = profileService.getMyTutorProfile(id);

        return ResponseEntity.ok(tutorProfileResponse);
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/tutor/me")
    public ResponseEntity<Void> postMyTutorProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorProfileRequest request
    ) {
        Long id = boardPrincipal.id();
        profileService.postMyTutorProfile(id, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/tutor/me")
    public ResponseEntity<Void> putMyTutorProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorProfileRequest request
    ) {
        Long id = boardPrincipal.id();
        profileService.putMyTutorProfile(id, request);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PatchMapping("/tutor/me")
    public ResponseEntity<Void> patchMyTutorProfile(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorProfilePatchRequest request
    ) {
        Long id = boardPrincipal.id();
        profileService.patchMyTutorProfile(id, request);

        return ResponseEntity.ok().build();
    }
}
