package com.cpa.yusin.quiz.product.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRegisterRequest
{
    private Integer durationMonths;
    private BigDecimal price;
}
