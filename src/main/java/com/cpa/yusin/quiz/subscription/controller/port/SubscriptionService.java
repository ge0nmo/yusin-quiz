package com.cpa.yusin.quiz.subscription.controller.port;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionService
{
    SubscriptionCreateResponse initiateSubscription(Member member, long subscriptionPlanId);

    void updateSubscriptionStatus(long memberId);

    GlobalResponse<List<SubscriptionDTO>> getSubscriptionHistory(long memberId, Pageable pageable);
}
