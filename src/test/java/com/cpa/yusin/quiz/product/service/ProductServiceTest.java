package com.cpa.yusin.quiz.product.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductDTO;
import com.cpa.yusin.quiz.product.controller.dto.response.ProductRegisterResponse;
import com.cpa.yusin.quiz.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.given;

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
                .durationMonth(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when
        ProductRegisterResponse result = testContainer.productService.save(request);

        // then
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.getDurationMonth()).isEqualTo(2);
    }

    @Test
    void update()
    {
        // given
        long productId = 1L;

        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .durationMonth(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when
        testContainer.productService.update(productId, request);

        // then
        Optional<Product> optionalProduct = testContainer.productRepository.findById(productId);
        assertThat(optionalProduct).isPresent();

        Product result = optionalProduct.get();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.getDurationMonth()).isEqualTo(2);

    }

    @Test
    void update_throwsExceptionWhenProductNotFound()
    {
        // given
        long productId = 5L;

        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .durationMonth(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when

        // then
        assertThatThrownBy(() -> testContainer.productService.findById(productId))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void findById()
    {
        // given
        long productId = 1L;

        // when
        Product result = testContainer.productService.findById(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(result.getDurationMonth()).isEqualTo(1);
    }

    @Test
    void findById_throwException()
    {
        // given
        long productId = 5L;

        // when

        // then
        assertThatThrownBy(() -> testContainer.productService.findById(productId))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void getById()
    {
        // given
        long productId = 1L;

        // when
        ProductDTO result = testContainer.productService.getById(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(result.getDurationMonth()).isEqualTo(1);
    }

    @Test
    void getAll_orderByDurationMonthASC()
    {
        // given

        // when
        List<ProductDTO> result = testContainer.productService.getAll();

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(4)
                .extracting(ProductDTO::getDurationMonth)
                .containsExactly(1, 3, 6, 12);

        assertThat(result)
                .extracting(ProductDTO::getPrice)
                .containsExactly(BigDecimal.valueOf(3000), BigDecimal.valueOf(6000), BigDecimal.valueOf(10000), BigDecimal.valueOf(15000));
    }

    @Test
    void deleteById()
    {
        // given
        long productId = 1L;

        // when
        testContainer.productService.deleteById(productId);

        // then
        Optional<Product> optionalProduct = testContainer.productRepository.findById(productId);
        assertThat(optionalProduct).isEmpty();
    }
}