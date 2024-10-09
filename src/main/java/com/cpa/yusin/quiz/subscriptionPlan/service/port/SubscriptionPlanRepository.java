package com.cpa.yusin.quiz.subscriptionPlan.service.port;

import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository
{
    SubscriptionPlan save(SubscriptionPlan subscriptionPlan);

    Optional<SubscriptionPlan> findById(Long id);

    List<SubscriptionPlan> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByDurationMonth(Integer durationMonth);

    boolean existsByDurationMonthAndIdNot(Integer durationMonth, Long id);
}
