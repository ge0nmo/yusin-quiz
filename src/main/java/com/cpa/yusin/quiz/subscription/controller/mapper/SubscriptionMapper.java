package com.cpa.yusin.quiz.subscription.controller.mapper;

import com.cpa.yusin.quiz.payment.controller.mapper.PaymentMapper;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SubscriptionMapper
{
    private final PaymentMapper paymentMapper;

    public SubscriptionCreateResponse toSubscriptionCreateResponse(Subscription subscription, Payment payment)
    {
        return SubscriptionCreateResponse.builder()
                .id(subscription.getId())
                .status(subscription.getStatus())
                .payment(paymentMapper.toPaymentRegisterResponse(payment))
                .build();
    }
}


