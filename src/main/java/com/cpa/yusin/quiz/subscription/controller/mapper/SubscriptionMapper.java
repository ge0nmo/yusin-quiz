package com.cpa.yusin.quiz.subscription.controller.mapper;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SubscriptionMapper
{
    public Subscription initiate(Member member, SubscriptionPlan plan, Payment payment)
    {
        LocalDateTime now = LocalDateTime.now();

        return Subscription.builder()
                .member(member)
                .plan(plan)
                .payment(payment)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .expiredDate(now.plusMonths(plan.getDurationMonth()))
                .build();
    }
}
