package com.cpa.yusin.quiz.choice.controller.port;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.config.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChoiceServiceTest extends MockSetup
{

    @Test
    void save()
    {
        // given
        ChoiceCreateRequest request1 = ChoiceCreateRequest.builder()
                .content("choice 1")
                .number(1)
                .isAnswer(false)
                .build();

        ChoiceCreateRequest request2 = ChoiceCreateRequest.builder()
                .content("choice 2")
                .number(2)
                .isAnswer(false)
                .build();

        ChoiceCreateRequest request3 = ChoiceCreateRequest.builder()
                .content("choice 3")
                .number(3)
                .isAnswer(true)
                .build();

        ChoiceCreateRequest request4 = ChoiceCreateRequest.builder()
                .content("choice 4")
                .number(4)
                .isAnswer(false)
                .build();

        List<ChoiceCreateRequest> requests = List.of(request1, request2, request3, request4);

        // when
        List<ChoiceCreateResponse> result = testContainer.choiceService.save(requests, physicsProblem2);

        // then
        assertThat(result).hasSize(4);
        assertThat(result.getFirst().getContent()).isEqualTo("choice 1");
        assertThat(result.getFirst().isAnswer()).isFalse();

        assertThat(result.get(2).isAnswer()).isTrue();
        assertThat(result.get(2).getContent()).isEqualTo("choice 3");

    }
}