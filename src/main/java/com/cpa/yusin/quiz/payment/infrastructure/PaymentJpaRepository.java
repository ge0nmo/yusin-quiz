package com.cpa.yusin.quiz.payment.infrastructure;

import com.cpa.yusin.quiz.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>
{
    Optional<Payment> findByMerchantUid(String merchantUid);
}
