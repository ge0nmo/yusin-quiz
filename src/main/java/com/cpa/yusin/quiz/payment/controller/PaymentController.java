package com.cpa.yusin.quiz.payment.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.payment.controller.port.PaymentService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/payment")
@Slf4j
@RequiredArgsConstructor
@RestController
public class PaymentController
{
    private final PaymentService paymentService;

    @GetMapping("/verify/{portOnePaymentId}")
    public ResponseEntity<GlobalResponse<IamportResponse<Payment>>> verifyPayment(@PathVariable("portOnePaymentId") String portOnePaymentId)
    {
        IamportResponse<Payment> paymentIamportResponse = paymentService.verifyPayment(portOnePaymentId);

        return ResponseEntity.ok(new GlobalResponse<>(paymentIamportResponse));
    }
}
