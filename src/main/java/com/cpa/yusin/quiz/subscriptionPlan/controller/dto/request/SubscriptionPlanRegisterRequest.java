package com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionPlanRegisterRequest
{
    @NotBlank
    private String name;

    @NotNull
    private Integer durationMonth;

    @NotNull
    private BigDecimal price;
}
