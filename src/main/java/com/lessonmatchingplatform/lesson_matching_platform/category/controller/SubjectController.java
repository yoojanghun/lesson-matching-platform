package com.lessonmatchingplatform.lesson_matching_platform.category.controller;

import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.SubjectCreateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.dto.request.SubjectUpdateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.category.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/subjects")
@RestController
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<Long> createSubject(
            @RequestBody @Valid SubjectCreateRequest request
    ) {
        Long subjectId = subjectService.createSubject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(subjectId);
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<Void> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody @Valid SubjectUpdateRequest request
    ) {
        subjectService.updateSubject(subjectId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable Long subjectId
    ) {
        subjectService.deleteSubject(subjectId);

        return ResponseEntity.noContent().build();
    }
}
