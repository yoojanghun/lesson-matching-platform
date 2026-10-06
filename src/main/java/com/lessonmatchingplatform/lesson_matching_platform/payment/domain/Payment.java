package com.lessonmatchingplatform.lesson_matching_platform.payment.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@Entity
public class Payment extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @Column(length = 64, nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer lessonCount;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 30, nullable = false)
    private PaymentStatus paymentStatus;

    @Column(length = 500)
    private String cancelReason;

    @Column(length = 50)
    private String tutorBankName;

    @Column(length = 30)
    private String tutorBankAccountNumber;

    @Column(length = 30)
    private String tutorBankAccountHolder;

    private LocalDateTime transferClaimedAt;
    private LocalDateTime confirmedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Reservation> reservationSet = new LinkedHashSet<>();

    protected Payment() {}

    private Payment(Matching matching, String orderId, Integer amount, Integer lessonCount,
                    String tutorBankName, String tutorBankAccountNumber, String tutorBankAccountHolder) {
        this.matching = matching;
        this.orderId = orderId;
        this.amount = amount;
        this.lessonCount = lessonCount;
        this.paymentStatus = PaymentStatus.PENDING_TRANSFER;
        this.tutorBankName = tutorBankName;
        this.tutorBankAccountNumber = tutorBankAccountNumber;
        this.tutorBankAccountHolder = tutorBankAccountHolder;
    }

    public static Payment of(Matching matching, String orderId, Integer amount, Integer lessonCount,
                             String tutorBankName, String tutorBankAccountNumber, String tutorBankAccountHolder) {
        return new Payment(matching, orderId, amount, lessonCount,
                tutorBankName, tutorBankAccountNumber, tutorBankAccountHolder);
    }


    public void addReservations(List<Reservation> reservations) {
        for (Reservation reservation : reservations) {
            this.getReservationSet().add(reservation);
            reservation.assignPayment(this);
        }
    }

    public void claimTransfer() {
        if (this.paymentStatus != PaymentStatus.PENDING_TRANSFER) {
            throw new IllegalStateException("이체 대기(PENDING_TRANSFER) 상태인 결제만 신고할 수 있습니다. 현재 상태: " + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.TRANSFER_CLAIMED;
        this.transferClaimedAt = LocalDateTime.now();
    }

    public void confirmByTutor() {
        if (this.paymentStatus != PaymentStatus.TRANSFER_CLAIMED) {
            throw new IllegalStateException("이체 완료 신고(TRANSFER_CLAIMED) 상태인 결제만 확인할 수 있습니다. 현재 상태: " + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.DONE;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancelByTutor(String cancelReason) {
        if (this.paymentStatus != PaymentStatus.TRANSFER_CLAIMED
                && this.paymentStatus != PaymentStatus.PENDING_TRANSFER) {
            throw new IllegalStateException("취소 가능한 상태가 아닙니다. 현재 상태: " + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelReason = cancelReason;
    }

    public void expire() {
        if (this.paymentStatus == PaymentStatus.PENDING_TRANSFER) {
            this.paymentStatus = PaymentStatus.EXPIRED;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment that)) return false;
        return this.paymentId != null && Objects.equals(this.paymentId, that.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(paymentId);
    }

}