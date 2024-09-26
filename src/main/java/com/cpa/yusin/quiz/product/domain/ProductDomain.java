package com.cpa.yusin.quiz.product.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class ProductDomain
{
    private Long id;
    private int durationMonth;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
