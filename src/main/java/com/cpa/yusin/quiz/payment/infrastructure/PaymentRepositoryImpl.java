package com.cpa.yusin.quiz.payment.infrastructure;

import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.service.port.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl implements PaymentRepository
{
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment)
    {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(long id)
    {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByMerchantUid(String merchantUid)
    {
        return paymentJpaRepository.findByMerchantUid(merchantUid);
    }
}
