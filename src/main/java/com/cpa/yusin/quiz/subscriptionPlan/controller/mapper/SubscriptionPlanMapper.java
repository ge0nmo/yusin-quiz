package com.cpa.yusin.quiz.subscriptionPlan.controller.mapper;

import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanRegisterRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanRegisterResponse;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPlanMapper
{
    public SubscriptionPlan toPlanEntity(SubscriptionPlanRegisterRequest request)
    {
        if(request == null)
            return null;

        return SubscriptionPlan.builder()
                .name(request.getName())
                .durationMonth(request.getDurationMonth())
                .price(request.getPrice())
                .build();
    }

    public SubscriptionPlanRegisterResponse toPlanRegisterResponse(SubscriptionPlan domain)
    {
        if(domain == null)
            return null;

        return SubscriptionPlanRegisterResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .durationMonth(domain.getDurationMonth())
                .price(domain.getPrice())
                .build();
    }

    public SubscriptionPlanDTO toPlanDTO(SubscriptionPlan domain)
    {
        if(domain == null)
            return null;

        return SubscriptionPlanDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .durationMonth(domain.getDurationMonth())
                .price(domain.getPrice())
                .build();
    }
}
