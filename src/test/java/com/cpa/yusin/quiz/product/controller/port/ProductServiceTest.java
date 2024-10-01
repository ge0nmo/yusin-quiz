package com.cpa.yusin.quiz.product.controller.port;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.productRepository.save(Product.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());
        testContainer.productRepository.save(Product.builder().id(2L).price(BigDecimal.valueOf(6000)).durationMonth(3).build());
        testContainer.productRepository.save(Product.builder().id(3L).price(BigDecimal.valueOf(10000)).durationMonth(6).build());
        testContainer.productRepository.save(Product.builder().id(4L).price(BigDecimal.valueOf(15000)).durationMonth(12).build());
    }

    @Test
    void save()
    {
        // given
        ProductRegisterRequest request = ProductRegisterRequest.builder()
                .durationMonths(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when
        ProductRegisterResponse result = testContainer.productService.save(request);

        // then
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.getDurationMonths()).isEqualTo(2);
    }

    @Test
    void update()
    {
        // given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .durationMonths(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when
        testContainer.productService.update(1L, request);

        // then
        Optional<Product> optionalProduct = testContainer.productRepository.findById(1L);
        assertThat(optionalProduct).isPresent();

        Product result = optionalProduct.get();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.getDurationMonth()).isEqualTo(2);

    }

    @Test
    void findById()
    {
        // given

        // when

        // then
    }

    @Test
    void getById()
    {
        // given

        // when

        // then
    }

    @Test
    void getAll()
    {
        // given

        // when

        // then
    }

    @Test
    void deleteById()
    {
        // given

        // when

        // then
    }
}