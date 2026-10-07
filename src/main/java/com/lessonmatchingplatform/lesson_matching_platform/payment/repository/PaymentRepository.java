package com.lessonmatchingplatform.lesson_matching_platform.payment.repository;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse;
import com.lessonmatchingplatform.lesson_matching_platform.payment.type.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.studentAccount s " +
            "JOIN FETCH s.userAccount su " +
            "JOIN FETCH m.tutorAccount t " +
            "JOIN FETCH t.userAccount tu " +
            "LEFT JOIN FETCH p.reservationSet r " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithDetail(@Param("orderId") String orderId);

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.studentAccount s " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithMatchingAndStudent(@Param("orderId") String orderId);

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.tutorAccount t " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithMatchingAndTutor(@Param("orderId") String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.studentAccount sa " +
            "LEFT JOIN FETCH p.reservationSet r " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdForStudentUpdate(@Param("orderId") String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.tutorAccount ta " +
            "LEFT JOIN FETCH p.reservationSet r " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdForTutorUpdate(@Param("orderId") String orderId);

    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.reservationSet r " +
            "WHERE p.paymentStatus = :status AND p.createdAt < :cutoff")
    List<Payment> findTimedOutPayments(@Param("status") PaymentStatus status, @Param("cutoff") LocalDateTime cutoff);

    @Query(
            value = "SELECT new com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse(" +
                    "p.paymentId, p.orderId, u.name, p.amount, p.lessonCount, p.paymentStatus, p.cancelReason, p.createdAt, p.transferClaimedAt, p.confirmedAt) " +
                    "FROM Payment p " +
                    "LEFT JOIN p.matching m " +
                    "LEFT JOIN m.tutorAccount t " +
                    "LEFT JOIN t.userAccount u " +
                    "WHERE m.studentAccount.studentId = :studentId",
            countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.matching m WHERE m.studentAccount.studentId = :studentId"
    )
    Page<PaymentListResponse> findStudentPayments(@Param("studentId") Long studentId, Pageable pageable);

    @Query(
            value = "SELECT new com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse(" +
                    "p.paymentId, p.orderId, u.name, p.amount, p.lessonCount, p.paymentStatus, p.cancelReason, p.createdAt, p.transferClaimedAt, p.confirmedAt) " +
                    "FROM Payment p " +
                    "LEFT JOIN p.matching m " +
                    "LEFT JOIN m.studentAccount s " +
                    "LEFT JOIN s.userAccount u " +
                    "WHERE m.tutorAccount.tutorId = :tutorId",
            countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.matching m WHERE m.tutorAccount.tutorId = :tutorId"
    )
    Page<PaymentListResponse> findTutorPayments(@Param("tutorId") Long tutorId, Pageable pageable);
}