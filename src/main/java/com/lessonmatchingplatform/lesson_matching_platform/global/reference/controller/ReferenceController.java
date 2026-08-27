package com.lessonmatchingplatform.lesson_matching_platform.global.reference.controller;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.global.reference.dto.response.ReferenceAllResponse;
import com.lessonmatchingplatform.lesson_matching_platform.global.reference.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/reference")
@RestController
public class ReferenceController {

    private final ReferenceService referenceService;

    @GetMapping("/profile")
    public ResponseEntity<ReferenceAllResponse> getAllReferences() {
        return ResponseEntity.ok(referenceService.getAllReferences());
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationDto>> getLocations() {
        return ResponseEntity.ok(referenceService.getLocations());
    }
}
