package com.cpa.yusin.quiz.payment.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentVerificationRequest
{
    private String portOnePaymentId;
    private String merchantId;
}
