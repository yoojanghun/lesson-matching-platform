package com.lessonmatchingplatform.lesson_matching_platform.payment.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentConfirmRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentPrepareRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentConfirmResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentPrepareResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.service.LessonPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/payments")
@RestController
public class LessonPaymentController {

    private final LessonPaymentService lessonPaymentService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid PaymentPrepareRequest request
    ) {
        Long studentId = boardPrincipal.id();
        PaymentPrepareResponse response = lessonPaymentService.preparePayment(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid PaymentConfirmRequest request
    ) {
        Long studentId = boardPrincipal.id();
        PaymentConfirmResponse response = lessonPaymentService.confirmPayment(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
