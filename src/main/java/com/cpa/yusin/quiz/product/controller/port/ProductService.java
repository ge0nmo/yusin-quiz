package com.cpa.yusin.quiz.product.controller.port;

import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.domain.Product;

import java.util.List;

public interface ProductService
{
    ProductRegisterResponse save(ProductRegisterRequest request);

    void update(long id, ProductUpdateRequest request);

    Product findById(Long id);

    ProductDTO getById(Long id);

    List<ProductDTO> getAll();

    void deleteById(Long id);
}
