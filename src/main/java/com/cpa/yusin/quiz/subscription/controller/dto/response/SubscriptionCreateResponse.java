package com.cpa.yusin.quiz.subscription.controller.dto.response;

import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentRegisterResponse;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SubscriptionCreateResponse(long id,
                                         SubscriptionStatus status,
                                         LocalDateTime createdAt,
                                         PaymentRegisterResponse payment,
                                         SubscriptionPlanDTO subscriptionPlan)
{
}
