package com.cpa.yusin.quiz.subscription.controller.port;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService
{
    SubscriptionCreateResponse initiateSubscription(Member member, long subscriptionPlanId);

    void updateSubscriptionStatus(long memberId);

    Page<SubscriptionDTO> getSubscriptionHistory(long memberId, Pageable pageable);
}
