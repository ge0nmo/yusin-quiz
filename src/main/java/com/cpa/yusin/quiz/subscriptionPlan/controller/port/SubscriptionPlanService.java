package com.cpa.yusin.quiz.subscriptionPlan.controller.port;

import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanRegisterRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanUpdateRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanRegisterResponse;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;

import java.util.List;

public interface SubscriptionPlanService
{
    SubscriptionPlanRegisterResponse save(SubscriptionPlanRegisterRequest request);

    void update(long id, SubscriptionPlanUpdateRequest request);

    SubscriptionPlan findById(Long id);

    SubscriptionPlanDTO getById(Long id);

    List<SubscriptionPlanDTO> getAll();

    void deleteById(Long id);
}
