package com.cpa.yusin.quiz.product.service.port;

public interface ProductValidator
{
    void validateDurationMonth(int durationMonth);

    void validateDurationMonth(long id, int durationMonth);
}
