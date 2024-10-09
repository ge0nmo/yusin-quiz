package com.cpa.yusin.quiz.subscriptionPlan.validator;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionPlanValidatorTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(1L).name("1개월 플랜").price(BigDecimal.valueOf(3000)).durationMonth(1).build());
        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(2L).name("3개월 플랜").price(BigDecimal.valueOf(6000)).durationMonth(3).build());
        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(3L).name("6개월 플랜").price(BigDecimal.valueOf(10000)).durationMonth(6).build());
        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(4L).name("1년 플랜").price(BigDecimal.valueOf(15000)).durationMonth(12).build());
    }

    @Test
    void validateDurationMonth()
    {
        // given
        int durationMonth = 4;

        // when
        testContainer.subscriptionPlanValidator.validateDurationMonth(durationMonth);

        // then

    }

    @Test
    void validateDurationMonth_throwExceptionWhenDurationMonthIsDuplicated()
    {
        // given
        int durationMonth = 3;

        // when

        // then
        assertThatThrownBy(() -> testContainer.subscriptionPlanValidator.validateDurationMonth(durationMonth))
                .isInstanceOf(GlobalException.class);

    }

    @Test
    void ValidateDurationMonthAndIdNot()
    {
        // given
        long id = 1;
        int durationMonth = 1;


        // when
        testContainer.subscriptionPlanValidator.validateDurationMonth(id, durationMonth);

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
        assertThatThrownBy(() -> testContainer.subscriptionPlanValidator.validateDurationMonth(id, durationMonth))
                .isInstanceOf(GlobalException.class);
    }
}