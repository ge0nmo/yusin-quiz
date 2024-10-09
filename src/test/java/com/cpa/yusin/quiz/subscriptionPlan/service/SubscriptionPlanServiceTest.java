package com.cpa.yusin.quiz.subscriptionPlan.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanRegisterRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanUpdateRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanDTO;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.response.SubscriptionPlanRegisterResponse;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionPlanServiceTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());
        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(2L).price(BigDecimal.valueOf(6000)).durationMonth(3).build());
        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(3L).price(BigDecimal.valueOf(10000)).durationMonth(6).build());
        testContainer.subscriptionPlanRepository.save(SubscriptionPlan.builder().id(4L).price(BigDecimal.valueOf(15000)).durationMonth(12).build());
    }

    @Test
    void save()
    {
        // given
        SubscriptionPlanRegisterRequest request = SubscriptionPlanRegisterRequest.builder()
                .durationMonth(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when
        SubscriptionPlanRegisterResponse result = testContainer.subscriptionPlanService.save(request);

        // then
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.getDurationMonth()).isEqualTo(2);
    }

    @Test
    void update()
    {
        // given
        long subscriptionPlanId = 1L;

        SubscriptionPlanUpdateRequest request = SubscriptionPlanUpdateRequest.builder()
                .durationMonth(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when
        testContainer.subscriptionPlanService.update(subscriptionPlanId, request);

        // then
        Optional<SubscriptionPlan> optionalProduct = testContainer.subscriptionPlanRepository.findById(subscriptionPlanId);
        assertThat(optionalProduct).isPresent();

        SubscriptionPlan result = optionalProduct.get();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.getDurationMonth()).isEqualTo(2);

    }

    @Test
    void update_throwsExceptionWhenProductNotFound()
    {
        // given
        long subscriptionPlanId = 5L;

        SubscriptionPlanUpdateRequest request = SubscriptionPlanUpdateRequest.builder()
                .durationMonth(2)
                .price(BigDecimal.valueOf(4000))
                .build();

        // when

        // then
        assertThatThrownBy(() -> testContainer.subscriptionPlanService.findById(subscriptionPlanId))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void findById()
    {
        // given
        long subscriptionPlanId = 1L;

        // when
        SubscriptionPlan result = testContainer.subscriptionPlanService.findById(subscriptionPlanId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(result.getDurationMonth()).isEqualTo(1);
    }

    @Test
    void findById_throwException()
    {
        // given
        long subscriptionPlanId = 5L;

        // when

        // then
        assertThatThrownBy(() -> testContainer.subscriptionPlanService.findById(subscriptionPlanId))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void getById()
    {
        // given
        long subscriptionPlanId = 1L;

        // when
        SubscriptionPlanDTO result = testContainer.subscriptionPlanService.getById(subscriptionPlanId);

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
        List<SubscriptionPlanDTO> result = testContainer.subscriptionPlanService.getAll();

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(4)
                .extracting(SubscriptionPlanDTO::getDurationMonth)
                .containsExactly(1, 3, 6, 12);

        assertThat(result)
                .extracting(SubscriptionPlanDTO::getPrice)
                .containsExactly(BigDecimal.valueOf(3000), BigDecimal.valueOf(6000), BigDecimal.valueOf(10000), BigDecimal.valueOf(15000));
    }

    @Test
    void deleteById()
    {
        // given
        long subscriptionPlanId = 1L;

        // when
        testContainer.subscriptionPlanService.deleteById(subscriptionPlanId);

        // then
        Optional<SubscriptionPlan> optionalProduct = testContainer.subscriptionPlanRepository.findById(subscriptionPlanId);
        assertThat(optionalProduct).isEmpty();
    }
}