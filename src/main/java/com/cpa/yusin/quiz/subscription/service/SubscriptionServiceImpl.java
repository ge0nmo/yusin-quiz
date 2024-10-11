package com.cpa.yusin.quiz.subscription.service;

import com.cpa.yusin.quiz.common.service.MerchantIdGenerator;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.SubscriptionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.payment.controller.mapper.PaymentMapper;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.service.port.PaymentRepository;
import com.cpa.yusin.quiz.subscription.controller.dto.response.SubscriptionCreateResponse;
import com.cpa.yusin.quiz.subscription.controller.mapper.SubscriptionMapper;
import com.cpa.yusin.quiz.subscription.controller.port.SubscriptionService;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionRepository;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PaymentMapper paymentMapper;

    @Transactional
    @Override
    public SubscriptionCreateResponse initiateSubscription(Member member, long subscriptionPlanId)
    {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new SubscriptionException(ExceptionMessage.SUBSCRIPTION_NOT_FOUND));

        String merchantId = merchantIdGenerator.generatePID(member.getId());
        Payment prePayment = Payment.initiate(subscriptionPlan.getPrice(), merchantId);
        prePayment = paymentRepository.save(prePayment);

        Subscription subscription = Subscription.initiate(member, subscriptionPlan, prePayment);

        subscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toSubscriptionCreateResponse(subscription, paymentMapper.toPaymentRegisterResponse(prePayment));
    }
}
