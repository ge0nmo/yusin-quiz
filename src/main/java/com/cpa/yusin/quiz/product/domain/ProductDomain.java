package com.cpa.yusin.quiz.product.domain;

import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
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

    public void update(ProductUpdateRequest request)
    {
        this.durationMonth = request.getDurationMonths();
        this.price = request.getPrice();
        this.updatedAt = LocalDateTime.now();
    }
}
