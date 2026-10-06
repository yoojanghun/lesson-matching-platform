package com.lessonmatchingplatform.lesson_matching_platform.payment.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReservationRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.ReservationStatus;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.PaymentPrepareRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.StudentCancelPaymentRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TutorBankAccountRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TutorCancelPaymentRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TutorConfirmPaymentRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request.TransferClaimRequest;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentDetailResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentPrepareResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentStatusResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.repository.PaymentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class LessonPaymentService {

    private final PaymentRepository paymentRepository;
    private final MatchingRepository matchingRepository;
    private final TutorsRepository tutorsRepository;
    private final ReservationRepository reservationRepository;

    // 학생이 결제 요청 생성
    @Transactional
    public PaymentPrepareResponse preparePayment(Long studentId, PaymentPrepareRequest request) {
        Matching matching = matchingRepository.findByMatchingIdAndStudentAccount_StudentId(request.matchingId(), studentId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 레슨 매칭이 없습니다."));

        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("결제가 불가능한 매칭 상태입니다.");
        }

        Integer pricePerLesson = matching.getPricePerLesson();
        if (pricePerLesson == null || pricePerLesson <= 0) {
            throw new IllegalStateException("선생님이 아직 레슨비를 설정하지 않은 매칭건입니다.");
        }

        TutorAccount tutor = matching.getTutorAccount();
        if (!tutor.hasBankAccount()) {
            throw new IllegalStateException("선생님이 아직 계좌 정보를 등록하지 않았습니다.");
        }

        List<Reservation> reservations = reservationRepository.findValidReservationsForPayment(
                request.reservationId(), request.matchingId(), ReservationStatus.COMPLETED
        );

        if (reservations.size() != request.reservationId().size()) {
            throw new IllegalArgumentException("결제할 수 없는 예약이 포함되어 있거나, 이미 결제 진행 중인 레슨입니다.");
        }

        Integer totalAmount = reservations.stream()
                .mapToInt(Reservation::getAppliedPrice)
                .sum();

        Payment payment = Payment.of(
                matching,
                generateOrderId(),
                totalAmount,
                reservations.size(),
                tutor.getBankName(),
                tutor.getBankAccountNumber(),
                tutor.getBankAccountHolder()
        );
        paymentRepository.save(payment);

        payment.addReservations(reservations);

        String orderName = String.format("%d회차 레슨", reservations.size());
        return PaymentPrepareResponse.of(payment, orderName);
    }

    // 학생이 이체 완료 신고
    @Transactional
    public PaymentStatusResponse claimTransfer(Long studentId, TransferClaimRequest request) {
        Payment payment = paymentRepository.findByOrderIdForStudentUpdate(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("해당되는 결제 정보가 없습니다."));

        if (!payment.getMatching().getStudentAccount().getStudentId().equals(studentId)) {
            throw new AccessDeniedException("해당 결제건에 대한 접근 권한이 없습니다.");
        }

        payment.claimTransfer();
        return PaymentStatusResponse.of(payment);
    }

    // 학생의 결제 취소 요청
    @Transactional
    public PaymentStatusResponse cancelPaymentByStudent(Long studentId, StudentCancelPaymentRequest request) {
        Payment payment = paymentRepository.findByOrderIdForStudentUpdate(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("해당되는 결제 정보가 없습니다."));

        if (!payment.getMatching().getStudentAccount().getStudentId().equals(studentId)) {
            throw new AccessDeniedException("해당 결제건에 대한 접근 권한이 없습니다.");
        }

        payment.cancelByStudent(request.cancelReason());
        return PaymentStatusResponse.of(payment);
    }

    // 선생님이 입금 확인 후, OK 처리
    @Transactional
    public PaymentStatusResponse confirmPaymentByTutor(Long tutorId, TutorConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findByOrderIdForTutorUpdate(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("해당되는 결제 정보가 없습니다."));

        if (!payment.getMatching().getTutorAccount().getTutorId().equals(tutorId)) {
            throw new AccessDeniedException("해당 결제건에 대한 접근 권한이 없습니다.");
        }

        payment.confirmByTutor();
        return PaymentStatusResponse.of(payment);
    }

    // 선생님의 결제 취소 처리
    @Transactional
    public PaymentStatusResponse cancelPaymentByTutor(Long tutorId, TutorCancelPaymentRequest request) {
        Payment payment = paymentRepository.findByOrderIdForTutorUpdate(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("해당되는 결제 정보가 없습니다."));

        if (!payment.getMatching().getTutorAccount().getTutorId().equals(tutorId)) {
            throw new AccessDeniedException("해당 결제건에 대한 접근 권한이 없습니다.");
        }

        payment.cancelByTutor(request.cancelReason());
        return PaymentStatusResponse.of(payment);
    }

    // 단건 결제 상세 조회
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(Long userId, String orderId) {
        Payment payment = paymentRepository.findByOrderIdWithDetail(orderId)
                .orElseThrow(() -> new EntityNotFoundException("해당되는 결제 정보가 없습니다."));

        Long studentId = payment.getMatching().getStudentAccount().getStudentId();
        Long tutorId = payment.getMatching().getTutorAccount().getTutorId();

        if (!userId.equals(studentId) && !userId.equals(tutorId)) {
            throw new AccessDeniedException("해당 결제 상세 정보를 조회할 권한이 없습니다.");
        }

        return PaymentDetailResponse.of(payment);
    }

    // 선생님이 계좌 정보 등록 / 수정
    @Transactional
    public void updateBankAccount(Long tutorId, TutorBankAccountRequest request) {
        TutorAccount tutor = tutorsRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("선생님 계정을 찾을 수 없습니다."));
        tutor.updateBankAccount(request.bankName(), request.bankAccountNumber(), request.bankAccountHolder());
    }

    // 학생이 자신의 결제 목록 조회
    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getStudentPayments(Long studentId, Pageable pageable) {
        return paymentRepository.findStudentPayments(studentId, pageable);
    }

    // 선생님 자신이 받아야 할 결제 목록 조회
    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getTutorPayments(Long tutorId, Pageable pageable) {
        return paymentRepository.findTutorPayments(tutorId, pageable);
    }

    // 24시간 초과된 미입금 건 자동 만료 처리
    @Transactional
    public void expireTimeoutPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Payment> timedOutPayments = paymentRepository.findTimedOutPayments(PaymentStatus.PENDING_TRANSFER, cutoff);

        for (Payment payment : timedOutPayments) {
            payment.expire();
        }
        if (!timedOutPayments.isEmpty()) {
            log.info("미입금 만료 배치 실행: 총 {}건 만료 처리 완료", timedOutPayments.size());
        }
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD_" + timestamp + "_" + randomStr;
    }
}