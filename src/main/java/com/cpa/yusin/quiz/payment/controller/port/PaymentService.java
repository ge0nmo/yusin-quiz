package com.cpa.yusin.quiz.payment.controller.port;

import com.cpa.yusin.quiz.payment.controller.dto.request.PaymentWebHookDTO;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

public interface PaymentService
{
    IamportResponse<Payment> verifyPayment(PaymentWebHookDTO webHookDTO);

    IamportResponse<Payment> verifyPayment(String portOnePaymentId);
}
