package com.cpa.yusin.quiz.payment.controller.dto.response;

import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRegisterResponse
{
    private long id;
    private BigDecimal amount;
    private PaymentStatus status;
    private String merchantUid;
}
