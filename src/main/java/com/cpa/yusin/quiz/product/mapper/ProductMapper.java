package com.cpa.yusin.quiz.product.mapper;

import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.domain.ProductDomain;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper
{
    public ProductDomain toProductDomain(ProductRegisterRequest request)
    {
        if(request == null)
            return null;

        return ProductDomain.builder()
                .durationMonth(request.getDurationMonths())
                .price(request.getPrice())
                .build();
    }

    public ProductRegisterResponse toProductRegisterResponse(ProductDomain domain)
    {
        if(domain == null)
            return null;

        return ProductRegisterResponse.builder()
                .id(domain.getId())
                .durationMonths(domain.getDurationMonth())
                .price(domain.getPrice())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public ProductDTO toProductDTO(ProductDomain domain)
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
