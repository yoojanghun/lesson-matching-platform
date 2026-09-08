package com.lessonmatchingplatform.lesson_matching_platform.payment.service;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.payment.client.TossPaymentsClient;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.exception.TossPaymentException;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;
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
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LessonPaymentService {

    private final PaymentRepository paymentRepository;
    private final MatchingRepository matchingRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentTransactionHandler paymentTransactionHandler;

    @Transactional
    public PaymentPrepareResponse preparePayment(Long studentId, PaymentPrepareRequest request) {
        Long matchingId = request.matchingId();
        Integer lessonCount = request.lessonCount();

        // 트랜잭션이 끝날 때까지 비관적 락을 걸어, 같은 결제가 2번 이상 반복되는 일이 없도록 함.
        Matching matching = matchingRepository.findByMatchingIdAndStudentAccount_StudentId(matchingId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 레슨이 없습니다."));

        // 매칭은 ACCEPTED인 경우에만 결제 가능
        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("결제가 불가능한 매칭 상태입니다.");
        }

        // 회당 레슨비는 Tutor가 설정
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

        // 1. [트랜잭션 1] 결제 전 유효성 검증 (READY 상태, 권한, 금액 일치 여부)
        paymentTransactionHandler.validatePaymentBeforeConfirm(orderId, studentId, amount);

        // 2. [외부 PG 호출 - 트랜잭션 외부] 토스페이먼츠 승인 요청 (서버 응답이 느릴 것을 대비해 DB 커넥션을 물지 않음)
        TossApproveResponse tossResponse;
        try {
            tossResponse = tossPaymentsClient.confirmPayment(request);
        } catch (TossPaymentException e) {
            // PG 승인 실패 시 실패 사유를 DB에 기록 (새 트랜잭션)
            paymentTransactionHandler.recordPaymentFailure(orderId, e.getMessage());
            throw e;
        } catch (Exception e) {
            paymentTransactionHandler.recordPaymentFailure(orderId, "알 수 없는 이유로 결제 승인에 실패했습니다.");
            throw e;
        }

        // 3. [트랜잭션 2] 결제 완료 상태 및 승인 정보 업데이트
        Payment paidPayment = paymentTransactionHandler.completePaymentSuccess(orderId, paymentKey, tossResponse);

        return PaymentConfirmResponse.of(paidPayment);
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD_" + timestamp + "_" + randomStr;
    }
}
