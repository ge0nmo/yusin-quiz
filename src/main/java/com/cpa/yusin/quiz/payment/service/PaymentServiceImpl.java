package com.cpa.yusin.quiz.payment.service;

import com.cpa.yusin.quiz.payment.controller.dto.request.PaymentWebHookDTO;
import com.cpa.yusin.quiz.payment.controller.port.PaymentService;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.payment.service.port.PaymentValidator;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionRepository;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService
{
    private final IamportClient iamportClient;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentValidator paymentValidator;

    @Override
    public IamportResponse<Payment> verifyPayment(PaymentWebHookDTO webHookDTO)
    {
        try {
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(webHookDTO.getPortOnePaymentId());
            String merchantUid = iamportResponse.getResponse().getMerchantUid();
            String portOnePaymentId = iamportResponse.getResponse().getImpUid();

            Subscription subscription = subscriptionRepository.findByMerchantId(merchantUid)
                    .orElseThrow();

            SubscriptionPlan subscriptionPlan = subscription.getPlan();

            com.cpa.yusin.quiz.payment.domain.Payment payment = subscription.getPayment(); // entity payment

            paymentValidator.validatePayment(iamportResponse.getResponse().getStatus(), iamportResponse.getResponse().getFailReason(),
                    iamportResponse.getResponse().getImpUid(), payment);
            paymentValidator.validatePrice(payment, iamportResponse.getResponse().getAmount(), portOnePaymentId);

            payment.completePayment(PaymentStatus.COMPLETED, "", portOnePaymentId);
            subscription.activeSubscription(subscriptionPlan.getDurationMonth());

            return iamportResponse;
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
