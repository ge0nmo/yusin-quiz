package com.cpa.yusin.quiz.subscription.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentRegisterResponse;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionServiceTest
{
    TestContainer testContainer;

    Member member;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        member = testContainer.memberRepository.save(Member.builder()
                .id(1L)
                .email("admin@gmail.com")
                .password("123123")
                .role(Role.ADMIN)
                .platform(Platform.HOME)
                .build());

        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .id(1L)
                .name("1개월 구독")
                .price(BigDecimal.valueOf(1000))
                .durationMonth(1)
                .build());

    }

    @Test
    void initiateSubscription()
    {
        // given
        long subscriptionId = 1L;

        // when
        SubscriptionCreateResponse result = testContainer.subscriptionService.initiateSubscription(member, subscriptionId);

        // then
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.PENDING);

        PaymentRegisterResponse payment = result.getPayment();
        System.out.println(payment.toString());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.valueOf(1000));

    }
}