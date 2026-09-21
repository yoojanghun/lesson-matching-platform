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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse;

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

    // 학생이 자신의 결제 정보를 확인할 수 있도록 함
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping
    public ResponseEntity<Page<PaymentListResponse>> getStudentPayments(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long studentId = boardPrincipal.id();
        Page<PaymentListResponse> response = lessonPaymentService.getStudentPayments(studentId, pageable);
        return ResponseEntity.ok(response);
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
