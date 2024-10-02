package com.cpa.yusin.quiz.product.validator;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductValidatorTest
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
    void validateDurationMonth()
    {
        // given
        int durationMonth = 4;

        // when
        testContainer.productValidator.validateDurationMonth(durationMonth);

        // then

    }

    @Test
    void validateDurationMonth_throwExceptionWhenDurationMonthIsDuplicated()
    {
        // given
        int durationMonth = 3;

        // when

        // then
        assertThatThrownBy(() -> testContainer.productValidator.validateDurationMonth(durationMonth))
                .isInstanceOf(GlobalException.class);

    }

    @Test
    void ValidateDurationMonthAndIdNot()
    {
        // given
        long id = 1;
        int durationMonth = 1;


        // when
        testContainer.productValidator.validateDurationMonth(id, durationMonth);

        // then
    }

    @Test
    void ValidateDurationMonthAndIdNot_throwExceptionWhenDurationMonthIsDuplicated()
    {
        // given
        long id = 2;
        int durationMonth = 1;


        // when

        // then
        assertThatThrownBy(() -> testContainer.productValidator.validateDurationMonth(id, durationMonth))
                .isInstanceOf(GlobalException.class);
    }
}