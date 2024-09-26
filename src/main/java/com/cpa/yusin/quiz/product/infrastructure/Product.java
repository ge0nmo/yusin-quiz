package com.cpa.yusin.quiz.product.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.product.domain.ProductDomain;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Product extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer durationMonth;

    @Column(nullable = false)
    private BigDecimal price;

    public static Product from(ProductDomain domain)
    {
        Product product = new Product();
        product.id = domain.getId();
        product.durationMonth = domain.getDurationMonth();
        product.price = domain.getPrice();
        product.setCreatedAt(domain.getCreatedAt());
        product.setUpdatedAt(domain.getUpdatedAt());

        return product;
    }

    public ProductDomain toModel()
    {
        return ProductDomain.builder()
                .id(this.id)
                .durationMonth(this.durationMonth)
                .price(this.price)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

}
