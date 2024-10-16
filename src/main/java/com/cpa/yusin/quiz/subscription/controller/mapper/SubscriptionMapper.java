package com.cpa.yusin.quiz.subscription.controller.mapper;

import com.cpa.yusin.quiz.payment.controller.mapper.PaymentMapper;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionDTO;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscriptionPlan.controller.mapper.SubscriptionPlanMapper;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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

    public SubscriptionDTO toSubscriptionDTO(Subscription subscription)
    {
        if(subscription == null){
            return null;
        }

        Payment payment = subscription.getPayment();
        SubscriptionPlan plan = subscription.getPlan();

        if(payment == null || plan == null){
            return null;
        }

        return SubscriptionDTO.builder()
                .id(subscription.getId())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .expiredDate(subscription.getExpiredDate())
                .paymentDTO(paymentMapper.toPaymentDTO(payment))
                .subscriptionPlan(subscriptionPlanMapper.toPlanDTO(plan))
                .build();
    }

    public List<SubscriptionDTO> toSubscriptionDTOList(List<Subscription> subscriptions)
    {
        if(subscriptions == null || subscriptions.isEmpty()){
            return Collections.emptyList();
        }

        return subscriptions.stream()
                .map(this::toSubscriptionDTO)
                .toList();
    }
}


