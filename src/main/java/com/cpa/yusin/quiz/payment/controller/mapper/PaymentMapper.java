package com.cpa.yusin.quiz.payment.controller.mapper;

import com.cpa.yusin.quiz.payment.controller.dto.response.PaymentRegisterResponse;
import com.cpa.yusin.quiz.payment.domain.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper
{
    public PaymentRegisterResponse toPaymentRegisterResponse(Payment payment)
    {
        return PaymentRegisterResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .merchantUid(payment.getMerchantUid())
                .status(payment.getStatus())
                .build();
    }
}
