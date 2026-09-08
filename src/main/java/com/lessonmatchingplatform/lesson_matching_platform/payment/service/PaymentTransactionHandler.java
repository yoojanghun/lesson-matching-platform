package com.lessonmatchingplatform.lesson_matching_platform.payment.service;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.TossApproveResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.repository.PaymentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentMethod;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Component
public class PaymentTransactionHandler {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void validatePaymentBeforeConfirm(String orderId, Long studentId, Integer amount) {
        Payment payment = paymentRepository.findByOrderIdWithMatchingAndStudentForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException("orderId에 해당되는 결제 정보가 없습니다."));

        if (!payment.getMatching().getStudentAccount().getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("해당 결제건에 대한 접근 권한이 없습니다.");
        }

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new IllegalStateException("결제 대기(READY) 상태인 주문만 승인할 수 있습니다. 현재 상태: " + payment.getPaymentStatus());
        }

        if (!amount.equals(payment.getAmount())) {
            payment.markAsFailed("결제 금액 위변조 감지");
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
    }

    @Transactional
    public Payment completePaymentSuccess(String orderId, String paymentKey, TossApproveResponse tossResponse) {
        Payment payment = paymentRepository.findByOrderIdWithMatchingAndStudent(orderId)
                .orElseThrow(() -> new EntityNotFoundException("orderId에 해당되는 결제 정보가 없습니다."));

        PaymentMethod paymentMethod = PaymentMethod.fromPgMethod(tossResponse.method());
        LocalDateTime approvedAt = OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime();

        payment.markAsPaid(paymentKey, paymentMethod, approvedAt);
        return payment;
    }

    // REQUIRES_NEW를 안 붙이고 부모 메서드가 하나의 트랜잭션 이라면, 에러가 발생하여 Rollback 할 때, "실패했다는 기록" 못 남기고 Rollback 같이 됨
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPaymentFailure(String orderId, String failReason) {
        paymentRepository.findByOrderIdWithMatchingAndStudent(orderId)
                .ifPresent(payment -> payment.markAsFailed(failReason));
    }
}
