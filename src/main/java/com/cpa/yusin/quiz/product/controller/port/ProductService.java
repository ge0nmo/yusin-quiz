package com.cpa.yusin.quiz.product.controller.port;

import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.domain.ProductDomain;

import java.util.List;

public interface ProductService
{
    ProductRegisterResponse save(ProductRegisterRequest request);

    ProductDomain findById(Long id);

    ProductDTO getById(Long id);

    List<ProductDTO> findAll();

    boolean deleteById(Long id);
}
