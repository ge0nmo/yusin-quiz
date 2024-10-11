package com.cpa.yusin.quiz.subscriptionPlan.infrastructure;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.PaymentException;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SubscriptionPlanValidatorImpl implements SubscriptionPlanValidator
{
    private final SubscriptionPlanRepository subscriptionPlanRepository;


    @Override
    public void validateDurationMonth(int durationMonth)
    {
        if(subscriptionPlanRepository.existsByDurationMonth(durationMonth)){
            throw new PaymentException(ExceptionMessage.PLAN_DUPLICATED);
        }
    }

    @Override
    public void validateDurationMonth(long id, int durationMonth)
    {
        if(subscriptionPlanRepository.existsByDurationMonthAndIdNot(durationMonth, id)){
            throw new PaymentException(ExceptionMessage.PLAN_DUPLICATED);
        }
    }
}
