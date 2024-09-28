package com.cpa.yusin.quiz.product.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest
{
    private Integer durationMonths;
    private BigDecimal price;
}
