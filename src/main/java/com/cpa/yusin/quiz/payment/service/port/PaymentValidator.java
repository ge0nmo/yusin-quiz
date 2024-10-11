package com.cpa.yusin.quiz.payment.service.port;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.PaymentException;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
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

    @Transactional(noRollbackFor = PaymentException.class)
    public void validatePrice(Payment payment, IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse)
    {
        com.siot.IamportRestClient.response.Payment response = iamportResponse.getResponse();
        BigDecimal paidAmount = response.getAmount();

        if (paidAmount.compareTo(payment.getAmount()) != 0) {
            try {
                iamportClient.cancelPaymentByImpUid(new CancelData(response.getImpUid(), true, paidAmount));

                payment.completePayment(PaymentStatus.FAILED,
                        "금액이 일치하지 않습니다.",
                                    response.getImpUid(),
                                    paidAmount,
                                    response.getPgProvider());

                throw new PaymentException(ExceptionMessage.PAYMENT_PRICE_ERROR);
            } catch (IamportResponseException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Transactional(noRollbackFor = PaymentException.class)
    public void validatePayment(IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse, Payment payment)
    {
        com.siot.IamportRestClient.response.Payment response = iamportResponse.getResponse();
        if (!response.getStatus().equals("paid")) {
            payment.completePayment(PaymentStatus.FAILED,
                                    response.getFailReason(),
                                    response.getImpUid(),
                                    response.getAmount(),
                                    response.getPgProvider());
            throw new PaymentException(ExceptionMessage.PAYMENT_NOT_COMPLETED);
        }
    }
}
