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
    @Override
    public Payment save(Payment payment)
    {
        return null;
    }

    @Override
    public Optional<Payment> findById(long id)
    {
        return Optional.empty();
    }
}
