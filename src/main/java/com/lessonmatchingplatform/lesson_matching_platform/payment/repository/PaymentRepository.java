package com.lessonmatchingplatform.lesson_matching_platform.payment.repository;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.studentAccount s " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithMatchingAndStudent(String orderId);

    // 동시성 방어용 비관적 락 조회 (SELECT FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.studentAccount sa " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithMatchingAndStudentForUpdate(String orderId);

    @Query(
            value = "SELECT new com.lessonmatchingplatform.lesson_matching_platform.payment.dto.response.PaymentListResponse(" +
                    "p.paymentId, p.orderId, u.name, p.amount, p.paymentStatus, p.createdAt, p.approvedAt) " +
                    "FROM Payment p " +
                    "JOIN p.matching m " +
                    "JOIN m.tutorAccount t " +
                    "JOIN t.userAccount u " +
                    "WHERE m.studentAccount.studentId = :studentId " +
                    "ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.matching m WHERE m.studentAccount.studentId = :studentId"
    )
    Page<PaymentListResponse> findStudentPayments(Long studentId, Pageable pageable);

}
