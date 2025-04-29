package com.cpa.yusin.quiz.choice.controller.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChoiceRequestTest {

    @Test
    void isNew_whenIdIsNull()
    {
        // given
        ChoiceRequest choiceRequest = ChoiceRequest.builder().id(null).build();

        // when
        boolean result = choiceRequest.isNew();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isNew_whenIdExists()
    {
        // given
        ChoiceRequest choiceRequest = ChoiceRequest.builder().id(10L).build();

        // when
        boolean result = choiceRequest.isNew();

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isRemoveYn_whenRemoveYnTrue()
    {
        // given
        ChoiceRequest choiceRequest = ChoiceRequest.builder().id(10L).removedYn(true).build();

        // when
        boolean result = choiceRequest.isRemovedYn();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isRemoveYn_whenRemoveYnFalse()
    {
        // given
        ChoiceRequest choiceRequest = ChoiceRequest.builder().id(10L).removedYn(false).build();

        // when
        boolean result = choiceRequest.isRemovedYn();

        // then
        assertThat(result).isFalse();
    }
}