package com.cpa.yusin.quiz.payment.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebHookDTO
{
    @JsonProperty("imp_uid")
    private String portOnePaymentId;

    @JsonProperty("merchant_uid")
    private String merchantId;

    private String status;
}
