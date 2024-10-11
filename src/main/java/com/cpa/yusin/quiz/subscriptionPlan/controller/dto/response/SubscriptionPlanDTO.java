package com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionPlanDTO
{
    private final long id;
    private final String name;
    private final int durationMonth;
    private final BigDecimal price;
}
