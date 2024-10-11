package com.cpa.yusin.quiz.payment.domain.type;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.PaymentException;
import lombok.Getter;

@Getter
public enum PaymentProvider
{
    KAKAO_PAY("kakaopay");

    private final String value;

    PaymentProvider(String value)
    {
        this.value = value;
    }

    public static PaymentProvider getPaymentProvider(String pgProvider)
    {
        for(PaymentProvider paymentProvider : PaymentProvider.values())
        {
            if(paymentProvider.value.equals(pgProvider)){
                return paymentProvider;
            }
        }
        throw new PaymentException(ExceptionMessage.PAYMENT_NOT_FOUND);
    }
}
