package com.cpa.yusin.quiz.payment.service.port;

import com.cpa.yusin.quiz.payment.domain.Payment;

import java.util.Optional;

public interface PaymentRepository
{
    Payment save(Payment payment);

    Optional<Payment> findById(long id);

    Optional<Payment> findByMerchantUid(String merchantUid);
}
