package com.cpa.yusin.quiz.subscriptionPlan.service.port;

public interface SubscriptionPlanValidator
{
    void validateDurationMonth(int durationMonth);

    void validateDurationMonth(long id, int durationMonth);
}
