package com.cpa.yusin.quiz.subscription.controller;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.details.MemberDetails;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionControllerTest
{
    TestContainer testContainer;

    Member member;

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

        plan = testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .id(1L)
                .name("1개월 구독")
                .price(BigDecimal.valueOf(1000))
                .durationMonth(1)
                .build());

    }


    @Test
    void subscribe()
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResponseEntity<?> result = testContainer.subscriptionController.subscribe(1L, memberDetails);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Optional<Subscription> optionalSubscription = testContainer.subscriptionRepository.findTopByMemberId(member.getId());
        assertTrue(optionalSubscription.isPresent());
        Subscription subscription = optionalSubscription.get();
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING);

        Payment payment = subscription.getPayment();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void checkStatus()
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

        // when
        ResponseEntity<?> result = testContainer.subscriptionController.checkStatus(member.getId());

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}