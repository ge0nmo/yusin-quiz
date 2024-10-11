package com.cpa.yusin.quiz.subscription.controller.dto.response;

import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentRegisterResponse;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import lombok.Builder;

@Builder
public record SubscriptionCreateResponse(long id, SubscriptionStatus status, PaymentRegisterResponse payment)
{
}
