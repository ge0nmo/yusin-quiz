package com.cpa.yusin.quiz.payment.infrastructure;

import com.cpa.yusin.quiz.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>
{
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN p.subscription " +
            "WHERE p.merchantUid = :merchantUid ")
    Optional<Payment> findByMerchantUid(@Param("merchantUid") String merchantUid);
}
