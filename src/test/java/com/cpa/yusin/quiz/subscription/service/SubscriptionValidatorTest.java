package com.cpa.yusin.quiz.subscription.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.SubscriptionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionValidatorTest
{
    TestContainer testContainer;

    Member member;
    Payment payment;
    SubscriptionPlan plan;

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

        plan = testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .id(1L)
                .name("1개월 플랜")
                .price(BigDecimal.valueOf(3000))
                .durationMonth(1)
                .build());

        String generatedId = testContainer.merchantIdGenerator.generateId(member.getId());

        payment = testContainer.paymentRepository.save(Payment.builder()
                .id(1L)
                .amount(plan.getPrice())
                .status(PaymentStatus.PENDING)
                .merchantUid(generatedId)
                .build());


    }

    @Test
    void validateSubscription_throwErrorIfSubscriptionIsAvailable()
    {
        // given
        long memberId = member.getId();

        testContainer.subscriptionRepository.save(
                Subscription.builder()
                        .id(1L)
                        .plan(plan)
                        .member(member)
                        .status(SubscriptionStatus.ACTIVE)
                        .payment(payment)
                        .startDate(LocalDateTime.now())
                        .expiredDate(LocalDateTime.now().plusDays(3))
                        .build());

        // when

        // then
        assertThatThrownBy(() -> testContainer.subscriptionValidator.validateSubscription(memberId))
                .isInstanceOf(SubscriptionException.class);
    }

    @Test
    void validateSubscription()
    {
        // given
        long memberId = member.getId();

        testContainer.subscriptionRepository.save(
                Subscription.builder()
                        .id(1L)
                        .plan(plan)
                        .member(member)
                        .status(SubscriptionStatus.ACTIVE)
                        .payment(payment)
                        .startDate(LocalDateTime.now())
                        .expiredDate(LocalDateTime.now().minusDays(3))
                        .build());

        // when

        // then
        testContainer.subscriptionValidator.validateSubscription(memberId);

    }



}