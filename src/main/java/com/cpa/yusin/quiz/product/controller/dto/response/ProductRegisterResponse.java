package com.cpa.yusin.quiz.product.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductRegisterResponse
{
    private final long id;
    private final int durationMonth;
    private final BigDecimal price;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
