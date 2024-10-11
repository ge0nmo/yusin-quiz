package com.cpa.yusin.quiz.payment.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.PaymentException;
import com.cpa.yusin.quiz.payment.controller.dto.request.PaymentWebHookDTO;
import com.cpa.yusin.quiz.payment.controller.port.PaymentService;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.payment.service.port.PaymentRepository;
import com.cpa.yusin.quiz.payment.service.port.PaymentValidator;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
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
    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final ClockHolder clockHolder;

    @Transactional(noRollbackFor = PaymentException.class)
    @Override
    public IamportResponse<Payment> verifyPayment(PaymentWebHookDTO webHookDTO)
    {
        try {
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(webHookDTO.getPortOnePaymentId());

            return verificationProcess(iamportResponse);
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(noRollbackFor = PaymentException.class)
    @Override
    public IamportResponse<Payment> verifyPayment(String portOnePaymentId)
    {
        try {
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(portOnePaymentId);

            return verificationProcess(iamportResponse);

        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IamportResponse<Payment> verificationProcess(IamportResponse<Payment> iamportResponse)
    {
        String merchantUid = iamportResponse.getResponse().getMerchantUid();

        com.cpa.yusin.quiz.payment.domain.Payment payment = findByMerchantUid(merchantUid);

        if(PaymentStatus.COMPLETED.equals(payment.getStatus())){
            return iamportResponse;
        }

        Subscription subscription = payment.getSubscription();
        SubscriptionPlan subscriptionPlan = subscription.getPlan();

        paymentValidator.validatePayment(iamportResponse, payment);

        paymentValidator.validatePrice(payment, iamportResponse);

        payment.completePayment(PaymentStatus.COMPLETED,
                "",
                iamportResponse.getResponse().getImpUid(),
                iamportResponse.getResponse().getAmount(),
                iamportResponse.getResponse().getPgProvider());
        subscription.activeSubscription(subscriptionPlan.getDurationMonth(), clockHolder.getCurrentDateTime());

        return iamportResponse;
    }

    public com.cpa.yusin.quiz.payment.domain.Payment findByMerchantUid(String merchantUid)
    {
        return paymentRepository.findByMerchantUid(merchantUid) // entity payment
                .orElseThrow(() -> new PaymentException(ExceptionMessage.PAYMENT_NOT_FOUND));
    }
}
