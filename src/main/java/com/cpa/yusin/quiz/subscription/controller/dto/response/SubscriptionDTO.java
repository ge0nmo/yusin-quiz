package com.cpa.yusin.quiz.subscription.controller.dto.response;

import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentDTO;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDTO
{
    private long id;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime expiredDate;
    private SubscriptionPlanDTO subscriptionPlan;
    private PaymentDTO payment;
}
