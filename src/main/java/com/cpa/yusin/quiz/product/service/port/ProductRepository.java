package com.cpa.yusin.quiz.product.service.port;

import com.cpa.yusin.quiz.product.domain.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
{
    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByDurationMonth(Integer durationMonth);

    boolean existsByDurationMonthAndIdNot(Integer durationMonth, Long id);
}
