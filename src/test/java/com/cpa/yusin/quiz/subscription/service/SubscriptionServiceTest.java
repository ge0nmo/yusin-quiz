package com.cpa.yusin.quiz.subscription.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentRegisterResponse;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionServiceTest
{
    TestContainer testContainer;

    Member member;
    Member subscriber;

    SubscriptionPlan plan;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        member = testContainer.memberRepository.save(Member.builder()
                .id(1L)
                .email("admin@gmail.com")
                .password("123123")
                .role(Role.USER)
                .platform(Platform.HOME)
                .build());

        subscriber = testContainer.memberRepository.save(Member.builder()
                .id(2L)
                .email("subscriber@gmail.com")
                .password("123123")
                .role(Role.SUBSCRIBER)
                .platform(Platform.HOME)
                .build());

        plan = testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder()
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

    @DisplayName("구독이 만료되었을 경우 유저의 상태를 변경한다")
    @Test
    void updateSubscriptionStatus_whenExpired()
    {
        // given
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = testContainer.subscriptionRepository.save(Subscription.builder()
                .id(1L)
                .member(member)
                .startDate(now.minusDays(5))
                .expiredDate(now.minusDays(1)) // 어제 만료
                .status(SubscriptionStatus.ACTIVE)
                .plan(plan)
                .build());


        testContainer.paymentRepository.save(Payment.builder()
                .id(1L)
                .status(PaymentStatus.COMPLETED)
                .amount(plan.getPrice())
                .merchantUid(testContainer.merchantIdGenerator.generateId(member.getId()))
                .paidAmount(plan.getPrice())
                .portOnePaymentId("1231231")
                .subscription(subscription)
                .build());

        member.changeRole(Role.SUBSCRIBER);
        testContainer.memberRepository.save(member);

        assertThat(member.getRole()).isEqualTo(Role.SUBSCRIBER);

        // when
        testContainer.subscriptionService.updateSubscriptionStatus(member.getId());

        // then
        Optional<Member> optionalMember = testContainer.memberRepository.findById(member.getId());
        assertThat(optionalMember).isNotEmpty();

        member = optionalMember.get();
        assertThat(member.getRole()).isEqualTo(Role.USER);

        Optional<Subscription> optionalSubscription = testContainer.subscriptionRepository.findById(subscription.getId());
        assertThat(optionalSubscription).isNotEmpty();
        subscription = optionalSubscription.get();
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @DisplayName("만료되지 않았거나 ADMIN 유저일 경우 변경하지 않는다")
    @Test
    void updateSubscriptionStatus_NoChange_whenNotExpired()
    {
        // given
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = testContainer.subscriptionRepository.save(Subscription.builder()
                .id(1L)
                .member(subscriber)
                .startDate(now.minusDays(5))
                .expiredDate(now.plusDays(5)) // 만료 X
                .status(SubscriptionStatus.ACTIVE)
                .plan(plan)
                .build());


        testContainer.paymentRepository.save(Payment.builder()
                .id(1L)
                .status(PaymentStatus.COMPLETED)
                .amount(plan.getPrice())
                .merchantUid(testContainer.merchantIdGenerator.generateId(subscriber.getId()))
                .paidAmount(plan.getPrice())
                .portOnePaymentId("1231231")
                .subscription(subscription)
                .build());

        member.changeRole(Role.SUBSCRIBER);
        testContainer.memberRepository.save(subscriber);

        assertThat(member.getRole()).isEqualTo(Role.SUBSCRIBER);

        // when
        testContainer.subscriptionService.updateSubscriptionStatus(subscriber.getId());

        // then
        Optional<Member> optionalMember = testContainer.memberRepository.findById(subscriber.getId());
        assertThat(optionalMember).isNotEmpty();

        subscriber = optionalMember.get();
        assertThat(subscriber.getRole()).isEqualTo(Role.SUBSCRIBER);

    }
}