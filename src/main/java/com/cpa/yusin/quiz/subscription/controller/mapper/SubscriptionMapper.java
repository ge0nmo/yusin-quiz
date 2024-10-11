package com.cpa.yusin.quiz.subscription.controller.mapper;

import com.cpa.yusin.quiz.payment.controller.mapper.PaymentMapper;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscriptionPlan.controller.mapper.SubscriptionPlanMapper;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SubscriptionMapper
{
    private final PaymentMapper paymentMapper;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    public SubscriptionCreateResponse toSubscriptionCreateResponse(Subscription subscription, Payment payment, SubscriptionPlan subscriptionPlan)
    {
        return SubscriptionCreateResponse.builder()
                .id(subscription.getId())
                .status(subscription.getStatus())
                .createdAt(subscription.getCreatedAt())
                .payment(paymentMapper.toPaymentRegisterResponse(payment))
                .subscriptionPlan(subscriptionPlanMapper.toPlanDTO(subscriptionPlan))
                .build();
    }
}


