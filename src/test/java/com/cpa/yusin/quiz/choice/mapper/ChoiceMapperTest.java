package com.cpa.yusin.quiz.choice.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.config.TestContainer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceMapperTest extends MockSetup
{
    TestContainer testContainer = new TestContainer();

    @Test
    void fromCreateRequestToDomain()
    {
        // given
        ChoiceCreateRequest request = ChoiceCreateRequest.builder()
                .number(1)
                .content("content1")
                .isAnswer(false)
                .build();

        // when
        ChoiceDomain result = testContainer.choiceMapper.fromCreateRequestToDomain(request, physicsProblem2);

        // then
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo("content1");
        assertThat(result.isAnswer()).isFalse();
    }

    @Test
    void fromCreateRequestToDomain_returnNull()
    {
        // given
        ChoiceCreateRequest request = null;

        // when
        ChoiceDomain result = testContainer.choiceMapper.fromCreateRequestToDomain(request, physicsProblem2);

        // then
        assertThat(result).isNull();
    }

    @Test
    void testFromCreateRequestsToDomains()
    {
        // given
        ChoiceCreateRequest request1 = ChoiceCreateRequest.builder()
                .number(1)
                .content("content1")
                .isAnswer(false)
                .build();

        ChoiceCreateRequest request2 = ChoiceCreateRequest.builder()
                .number(2)
                .content("content2")
                .isAnswer(false)
                .build();

        ChoiceCreateRequest request3 = ChoiceCreateRequest.builder()
                .number(3)
                .content("content3")
                .isAnswer(true)
                .build();

        List<ChoiceCreateRequest> request = List.of(request1, request2, request3);

        // when
        List<ChoiceDomain> result = testContainer.choiceMapper.fromCreateRequestToDomain(request, physicsProblem2);

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void testFromCreateRequestsToDomains_returnEmptyList()
    {
        // given
        List<ChoiceCreateRequest> request = new ArrayList<>();

        // when
        List<ChoiceDomain> result = testContainer.choiceMapper.fromCreateRequestToDomain(request, physicsProblem2);

        // then
        assertThat(result).isEmpty();
        assertThat(result).hasSize(0);
    }

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

        // when

        // then
    }
}