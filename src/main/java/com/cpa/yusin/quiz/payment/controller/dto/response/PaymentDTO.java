package com.cpa.yusin.quiz.payment.controller.dto.response;

import com.cpa.yusin.quiz.payment.domain.type.PaymentProvider;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO
{
    private long id;
    private BigDecimal amount;
    private PaymentStatus status;
    private String portOnePaymentId;
    private String merchantUid;
    private PaymentProvider paymentProvider;
    private String failureReason;

}
