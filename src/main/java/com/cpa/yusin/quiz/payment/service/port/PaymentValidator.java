package com.cpa.yusin.quiz.payment.service.port;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.PaymentException;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;


@RequiredArgsConstructor
@Component
public class PaymentValidator
{
    private final IamportClient iamportClient;

    @Transactional
    public void validatePrice(Payment payment, BigDecimal paidAmount, String portOnePaymentId)
    {
        if(paidAmount.compareTo(payment.getAmount()) != 0){
            try {
                iamportClient.cancelPaymentByImpUid(new CancelData(portOnePaymentId, true, paidAmount));
                payment.completePayment(PaymentStatus.FAILED, "금액이 일치하지 않습니다.", portOnePaymentId);
                throw new PaymentException(ExceptionMessage.PAYMENT_PRICE_ERROR);
            } catch (IamportResponseException | IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void validatePayment(String paymentStatus, String failureMessage, String portOnePaymentId, Payment payment)
    {
        if(!paymentStatus.equals("paid")){
            payment.completePayment(PaymentStatus.FAILED, failureMessage, portOnePaymentId);
            throw new PaymentException(ExceptionMessage.PAYMENT_NOT_COMPLETED);
        }
    }
}
