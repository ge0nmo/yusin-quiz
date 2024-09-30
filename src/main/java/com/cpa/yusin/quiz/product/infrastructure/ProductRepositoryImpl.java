package com.cpa.yusin.quiz.product.infrastructure;

import com.cpa.yusin.quiz.product.domain.Product;
import com.cpa.yusin.quiz.product.service.port.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryImpl implements ProductRepository
{
    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository)
    {
        this.productJpaRepository = productJpaRepository;
    }


    @Override
    public Product save(Product product)
    {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id)
    {
        return productJpaRepository.findById(id);
    }

    @Override
    public List<Product> findAll()
    {
        return productJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id)
    {
        productJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id)
    {
        return productJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByDurationMonth(Integer durationMonth)
    {
        return productJpaRepository.existsByDurationMonth(durationMonth);
    }

    @Override
    public boolean existsByDurationMonthAndIdNot(Integer durationMonth, Long id)
    {
        return productJpaRepository.existsByDurationMonthAndIdNot(durationMonth, id);
    }


}
