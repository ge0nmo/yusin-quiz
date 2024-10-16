package com.cpa.yusin.quiz.subscription.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.common.service.MerchantIdGenerator;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.SubscriptionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.service.port.PaymentRepository;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionDTO;
import com.cpa.yusin.quiz.subscription.controller.mapper.SubscriptionMapper;
import com.cpa.yusin.quiz.subscription.controller.port.SubscriptionService;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionRepository;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionValidator;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionServiceImpl implements SubscriptionService
{
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final MerchantIdGenerator merchantIdGenerator;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionValidator subscriptionValidator;
    private final ClockHolder clockHolder;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public SubscriptionCreateResponse initiateSubscription(Member member, long subscriptionPlanId)
    {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new SubscriptionException(ExceptionMessage.SUBSCRIPTION_NOT_FOUND));

        subscriptionValidator.validateSubscription(member.getId());

        String merchantId = merchantIdGenerator.generateId(member.getId());
        Payment prePayment = Payment.initiate(subscriptionPlan.getPrice(), merchantId);
        prePayment = paymentRepository.save(prePayment);

        Subscription subscription = Subscription.initiate(member, subscriptionPlan, prePayment);

        subscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toSubscriptionCreateResponse(subscription, prePayment, subscriptionPlan);
    }

    @Transactional
    @Override
    public void updateSubscriptionStatus(long memberId)
    {
        findTopByMemberId(memberId)
                .ifPresent(subscription -> {
                    subscription.updateSubscriptionStatus(clockHolder.getCurrentDateTime());
                    subscriptionRepository.save(subscription);
                    memberRepository.save(subscription.getMember());
                });
    }

    @Override
    public Page<SubscriptionDTO> getSubscriptionHistory(long memberId, Pageable pageable)
    {
        subscriptionRepository.findAllByMemberId(memberId, pageable);

        return null;
    }


    private Optional<Subscription> findTopByMemberId(long memberId){
        return subscriptionRepository.findTopByMemberId(memberId);
    }
}
