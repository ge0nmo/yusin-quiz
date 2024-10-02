package com.cpa.yusin.quiz.product.controller.mapper;

import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper
{
    public Product toProductDomain(ProductRegisterRequest request)
    {
        if(request == null)
            return null;

        return Product.builder()
                .durationMonth(request.getDurationMonth())
                .price(request.getPrice())
                .build();
    }

    public ProductRegisterResponse toProductRegisterResponse(Product domain)
    {
        if(domain == null)
            return null;

        return ProductRegisterResponse.builder()
                .id(domain.getId())
                .durationMonth(domain.getDurationMonth())
                .price(domain.getPrice())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public ProductDTO toProductDTO(Product domain)
    {
        if(domain == null)
            return null;

        return ProductDTO.builder()
                .id(domain.getId())
                .durationMonth(domain.getDurationMonth())
                .price(domain.getPrice())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
