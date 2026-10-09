package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentDetailResponse(
        Long paymentId,
        String orderId,
        Long matchingId,
        String tutorName,
        String studentName,
        Integer amount,
        Integer lessonCount,
        PaymentStatus paymentStatus,
        String cancelReason,
        String tutorBankName,
        String tutorBankAccountNumber,
        String tutorBankAccountHolder,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime transferClaimedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime confirmedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        List<PaymentReservationItem> reservations
) {

    public static PaymentDetailResponse of(Payment payment) {
        Matching matching = payment.getMatching();
        Long matchingId = matching.getMatchingId();
        UserAccount tutorUserAccount = payment.getMatching().getTutorAccount().getUserAccount();
        UserAccount studentUserAccount = payment.getMatching().getStudentAccount().getUserAccount();

        String tutorName = tutorUserAccount != null ? tutorUserAccount.getName() : null;
        String studentName = studentUserAccount != null ? studentUserAccount.getName() : null;

        List<PaymentReservationItem> items = payment.getReservationSet().stream()
                .map(PaymentReservationItem::from)
                .toList();

        return new PaymentDetailResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                matchingId,
                tutorName,
                studentName,
                payment.getAmount(),
                payment.getLessonCount(),
                payment.getPaymentStatus(),
                payment.getCancelReason(),
                payment.getTutorBankName(),
                payment.getTutorBankAccountNumber(),
                payment.getTutorBankAccountHolder(),
                payment.getTransferClaimedAt(),
                payment.getConfirmedAt(),
                payment.getCreatedAt(),
                items
        );
    }
}