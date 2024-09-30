package com.cpa.yusin.quiz.product.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Product extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer durationMonth;

    @Column(nullable = false)
    private BigDecimal price;

    public void update(ProductUpdateRequest request)
    {
        this.durationMonth = request.getDurationMonths();
        this.price = request.getPrice();
    }
}
