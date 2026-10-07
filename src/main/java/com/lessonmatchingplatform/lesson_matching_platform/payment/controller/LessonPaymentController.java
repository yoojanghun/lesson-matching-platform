package com.lessonmatchingplatform.lesson_matching_platform.payment.controller;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentPrepareRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.StudentCancelPaymentRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TransferClaimRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TutorBankAccountRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TutorCancelPaymentRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TutorConfirmPaymentRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentDetailResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentPrepareResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentStatusResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.service.LessonPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/payments")
@RestController
public class LessonPaymentController {

    private final LessonPaymentService lessonPaymentService;

    // 학생이 결제 요청 생성
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid PaymentPrepareRequest request
    ) {
        PaymentPrepareResponse response = lessonPaymentService.preparePayment(
                boardPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 학생이 이체 완료 신고
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/claim-transfer")
    public ResponseEntity<PaymentStatusResponse> claimTransfer(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TransferClaimRequest request
    ) {
        PaymentStatusResponse response = lessonPaymentService.claimTransfer(
                boardPrincipal.id(), request);
        return ResponseEntity.ok(response);
    }

    // 학생의 결제 취소 요청
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/cancel-by-student")
    public ResponseEntity<PaymentStatusResponse> cancelPaymentByStudent(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid StudentCancelPaymentRequest request
    ) {
        PaymentStatusResponse response = lessonPaymentService.cancelPaymentByStudent(
                boardPrincipal.id(), request);
        return ResponseEntity.ok(response);
    }

    // 단건 결제 상세 조회
    @PreAuthorize("hasAnyRole('STUDENT', 'TUTOR')")
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentDetailResponse> getPaymentDetail(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PathVariable String orderId
    ) {
        PaymentDetailResponse response = lessonPaymentService.getPaymentDetail(
                boardPrincipal.id(), orderId);
        return ResponseEntity.ok(response);
    }

    // 학생이 자신의 결제 목록 조회
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping
    public ResponseEntity<Page<PaymentListResponse>> getStudentPayments(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                lessonPaymentService.getStudentPayments(boardPrincipal.id(), pageable));
    }

    // 선생님이 계좌 정보 등록 / 수정
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/bank-account")
    public ResponseEntity<Void> updateBankAccount(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorBankAccountRequest request
    ) {
        lessonPaymentService.updateBankAccount(boardPrincipal.id(), request);
        return ResponseEntity.ok().build();
    }

    // 선생님이 입금 확인 후, OK 처리
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentStatusResponse> confirmPayment(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorConfirmPaymentRequest request
    ) {
        PaymentStatusResponse response = lessonPaymentService.confirmPaymentByTutor(
                boardPrincipal.id(), request);
        return ResponseEntity.ok(response);
    }

    // 선생님의 결제 취소 처리
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/cancel")
    public ResponseEntity<PaymentStatusResponse> cancelPayment(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @RequestBody @Valid TutorCancelPaymentRequest request
    ) {
        PaymentStatusResponse response = lessonPaymentService.cancelPaymentByTutor(
                boardPrincipal.id(), request);
        return ResponseEntity.ok(response);
    }

    // 선생님 자신이 받아야 할 결제 목록 조회
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/tutor")
    public ResponseEntity<Page<PaymentListResponse>> getTutorPayments(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                lessonPaymentService.getTutorPayments(boardPrincipal.id(), pageable));
    }
}