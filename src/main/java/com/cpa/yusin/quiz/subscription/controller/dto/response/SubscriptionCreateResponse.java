package com.cpa.yusin.quiz.subscription.controller.dto.response;

import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentRegisterResponse;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SubscriptionCreateResponse
{
    private long id;
    private SubscriptionStatus status;
    private LocalDateTime createdAt;
    private PaymentRegisterResponse payment;
    private SubscriptionPlanDTO subscriptionPlan;
}
