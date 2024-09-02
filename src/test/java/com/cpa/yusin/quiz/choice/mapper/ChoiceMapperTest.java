package com.cpa.yusin.quiz.choice.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.config.TestContainer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceMapperTest extends MockSetup
{
    TestContainer testContainer = new TestContainer();


    @Test
    void toCreateResponse()
    {
        // given
        ChoiceDomain domain = ChoiceDomain.builder()
                .id(1L)
                .content("choice1")
                .number(1)
                .isAnswer(true)
                .problem(physicsProblem1)
                .build();
        // when
        ChoiceCreateResponse result = testContainer.choiceMapper.toCreateResponse(domain);

        // then
        assertThat(result.getContent()).isEqualTo("choice1");
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.isAnswer()).isTrue();
    }

    @Test
    void toCreateResponses()
    {
        // given
        ChoiceDomain choice1 = ChoiceDomain.builder()
                .id(1L)
                .content("choice 1")
                .number(1)
                .isAnswer(true)
                .problem(physicsProblem1)
                .build();

        ChoiceDomain choice2 = ChoiceDomain.builder()
                .id(2L)
                .content("choice 2")
                .number(2)
                .isAnswer(false)
                .problem(physicsProblem1)
                .build();

        ChoiceDomain choice3 = ChoiceDomain.builder()
                .id(3L)
                .content("choice 3")
                .number(3)
                .isAnswer(false)
                .problem(physicsProblem1)
                .build();


        List<ChoiceDomain> request = List.of(choice1, choice2, choice3);

        // when
        List<ChoiceCreateResponse> result = testContainer.choiceMapper.toCreateResponses(request);

        // then
        assertThat(result).hasSize(3);

        assertThat(result.getFirst().getNumber()).isEqualTo(1);
        assertThat(result.getFirst().getContent()).isEqualTo("choice 1");
        assertThat(result.getFirst().isAnswer()).isTrue();

        assertThat(result.get(1).getNumber()).isEqualTo(2);
        assertThat(result.get(1).getContent()).isEqualTo("choice 2");
        assertThat(result.get(1).isAnswer()).isFalse();

        assertThat(result.get(2).getNumber()).isEqualTo(3);
        assertThat(result.get(2).getContent()).isEqualTo("choice 3");
        assertThat(result.get(2).isAnswer()).isFalse();
    }

    @Test
    void toCreateResponses_returnEmptyList()
    {
        // given
        List<ChoiceDomain> request = null;

        // when

        List<ChoiceCreateResponse> result = testContainer.choiceMapper.toCreateResponses(request);

        // then
        assertThat(result).isEmpty();
    }
}