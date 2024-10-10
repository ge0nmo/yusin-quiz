package com.cpa.yusin.quiz.subscription.controller.port;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;

public interface SubscriptionService
{
    SubscriptionCreateResponse initiateSubscription(Member member, long subscriptionPlanId);
}
