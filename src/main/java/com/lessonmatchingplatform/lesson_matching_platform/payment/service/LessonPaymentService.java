package com.lessonmatchingplatform.lesson_matching_platform.payment.service;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.payment.client.TossPaymentsClient;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.PaymentMethod;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.PaymentStatus;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentConfirmRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentPrepareRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentConfirmResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentPrepareResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.TossApproveResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Service
public class LessonPaymentService {

    private final PaymentRepository paymentRepository;
    private final MatchingRepository matchingRepository;
    private final TossPaymentsClient tossPaymentsClient;

    public PaymentPrepareResponse preparePayment(Long studentId, PaymentPrepareRequest request) {
        Long matchingId = request.matchingId();
        Integer lessonCount = request.lessonCount();

        Matching matching = matchingRepository.findByMatchingIdAndStudentAccount_StudentId(matchingId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 레슨이 없습니다."));

        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("결제가 불가능한 매칭 상태입니다.");
        }

        Integer pricePerLesson = matching.getPricePerLesson();

        if (pricePerLesson == null || pricePerLesson <= 0) {
            throw new IllegalStateException("선생님이 아직 레슨비를 설정하지 않은 매칭건입니다.");
        }

        Integer totalAmount = lessonCount * pricePerLesson;

        Payment payment = Payment.of(
                matching,
                generateOrderId(),
                totalAmount,
                PaymentStatus.READY
        );

        paymentRepository.save(payment);

        String orderName = String.format("%d회차 레슨", request.lessonCount());

        return PaymentPrepareResponse.of(
                payment,
                orderName
        );
    }

    public PaymentConfirmResponse confirmPayment(Long studentId, PaymentConfirmRequest request) {
        String orderId = request.orderId();
        String paymentKey = request.paymentKey();
        Integer amount = request.amount();

        Payment payment = paymentRepository.findByOrderIdWithMatchingAndStudent(orderId)
                .orElseThrow(() -> new EntityNotFoundException("orderId에 해당되는 결제 정보가 없습니다."));

        if (!payment.getMatching().getStudentAccount().getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("해당 결제건에 대한 접근 권한이 없습니다.");
        }

        if (!amount.equals(payment.getAmount())) {
            payment.markAsFailed("결제 금액 위변조 감지");
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        TossApproveResponse tossResponse = tossPaymentsClient.confirmPayment(request);
        PaymentMethod paymentMethod = PaymentMethod.fromPgMethod(tossResponse.method());
        LocalDateTime approvedAt = OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime();

        payment.markAsPaid(paymentKey, paymentMethod, approvedAt);

        return PaymentConfirmResponse.of(payment);
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD_" + timestamp + "_" + randomStr;
    }

}
