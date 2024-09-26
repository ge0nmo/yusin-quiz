package com.cpa.yusin.quiz.product.service.port;

import com.cpa.yusin.quiz.product.domain.ProductDomain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
{
    ProductDomain save(ProductDomain product);

    Optional<ProductDomain> findById(Long id);

    List<ProductDomain> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByDurationMonth(Integer durationMonth);

    boolean existsByDurationMonthAndIdNot(Integer durationMonth, Long id);
}
