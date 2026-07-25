package com.lessonmatchingplatform.lesson_matching_platform.payment.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Column(length = 200)
    private String paymentKey;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus paymentStatus;

    @Column(length = 255)
    private String failReason;

    private LocalDateTime approvedAt;

    protected Payment() {}

    private Payment (Matching matching, String orderId, Integer amount, PaymentStatus paymentStatus) {
        this.matching = matching;
        this.orderId = orderId;
        this.paymentKey = null;
        this.amount = amount;
        this.paymentMethod = null;
        this.paymentStatus = paymentStatus;
        this.failReason = null;
        this.approvedAt = null;
    }

    public static Payment of(Matching matching, String orderId, Integer amount, PaymentStatus paymentStatus) {
        return new Payment(matching, orderId, amount, paymentStatus);
    }

    public void markAsPaid(String paymentKey, PaymentMethod paymentMethod, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.DONE;
        this.approvedAt = approvedAt;
    }

    public void markAsFailed(String failReason){
        this.failReason = failReason;
        this.paymentStatus = PaymentStatus.FAILED;
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
