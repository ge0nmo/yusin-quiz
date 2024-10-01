package com.cpa.yusin.quiz.product.infrastructure;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.product.service.port.ProductRepository;
import com.cpa.yusin.quiz.product.service.port.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductValidatorImpl implements ProductValidator
{
    private final ProductRepository productRepository;


    @Override
    public void validateDurationMonth(int durationMonth)
    {
        if(productRepository.existsByDurationMonth(durationMonth)){
            throw new GlobalException(ExceptionMessage.PRODUCT_DUPLICATED);
        }
    }

    @Override
    public void validateDurationMonth(long id, int durationMonth)
    {
        if(productRepository.existsByDurationMonthAndIdNot(durationMonth, id)){
            throw new GlobalException(ExceptionMessage.PRODUCT_DUPLICATED);
        }
    }
}
