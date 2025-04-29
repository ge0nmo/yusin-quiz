package com.cpa.yusin.quiz.problem.controller.dto.request;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProblemRequestTest {

    @Test
    void isNew_shouldReturnFalse()
    {
        // given
        ProblemRequest request = ProblemRequest.builder().id(-1L).content("내용")
                .choices(new ArrayList<>()).number(1).explanation("설명").build();

        // when
        boolean result = request.isNew();

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isNew_shouldReturnTrue()
    {
        // given
        ProblemRequest request = ProblemRequest.builder().id(10L).content("내용")
                .choices(new ArrayList<>()).number(1).explanation("설명").build();

        // when
        boolean result = request.isNew();

        // then
        assertThat(result).isFalse();
    }
}