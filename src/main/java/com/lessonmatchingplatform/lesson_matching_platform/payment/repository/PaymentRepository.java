package com.lessonmatchingplatform.lesson_matching_platform.payment.repository;

import com.lessonmatchingplatform.lesson_matching_platform.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.matching m " +
            "JOIN FETCH m.studentAccount s " +
            "WHERE p.orderId = :orderId")
    Optional<Payment> findByOrderIdWithMatchingAndStudent(String orderId);
}
