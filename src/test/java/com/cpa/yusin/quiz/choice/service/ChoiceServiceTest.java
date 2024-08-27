package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void update()
    {
        // given
        Map<Long, List<ChoiceUpdateRequest>> updateMaps = new HashMap<>();
        ChoiceUpdateRequest choice1 = ChoiceUpdateRequest.builder()
                .id(1L)
                .number(1)
                .content("update 1")
                .isDeleted(false)
                .isAnswer(false)
                .build();

        ChoiceUpdateRequest choice2 = ChoiceUpdateRequest.builder()
                .id(2L)
                .number(2)
                .content("update 2")
                .isDeleted(false)
                .isAnswer(false)
                .build();

        ChoiceUpdateRequest choice3 = ChoiceUpdateRequest.builder()
                .id(3L)
                .number(3)
                .content("update 3")
                .isDeleted(false)
                .isAnswer(false)
                .build();

        List<ChoiceUpdateRequest> request = List.of(choice1, choice2, choice3);
        updateMaps.put(1L, request);

        // when
        testContainer.choiceService.update(updateMaps);

        // then
        List<ChoiceDomain> result = testContainer.choiceRepository.findAllByProblemId(1L);

        assertThat(result.get(0).getContent()).isEqualTo("update 1");
        assertThat(result.get(1).getContent()).isEqualTo("update 2");
        assertThat(result.get(2).getContent()).isEqualTo("update 3");
    }

    @Test
    void update_remove()
    {
        // given
        Map<Long, List<ChoiceUpdateRequest>> updateMaps = new HashMap<>();
        ChoiceUpdateRequest choice1 = ChoiceUpdateRequest.builder()
                .id(1L)
                .number(1)
                .content("update 1")
                .isDeleted(true)
                .isAnswer(false)
                .build();

        ChoiceUpdateRequest choice2 = ChoiceUpdateRequest.builder()
                .id(2L)
                .number(2)
                .content("update 2")
                .isDeleted(true)
                .isAnswer(false)
                .build();

        ChoiceUpdateRequest choice3 = ChoiceUpdateRequest.builder()
                .id(3L)
                .number(3)
                .content("update 3")
                .isDeleted(false)
                .isAnswer(false)
                .build();

        List<ChoiceUpdateRequest> request = List.of(choice1, choice2, choice3);
        updateMaps.put(1L, request);

        // when
        testContainer.choiceService.update(updateMaps);

        // then
        List<ChoiceDomain> result = testContainer.choiceRepository.findAllByProblemId(1L);
        result.forEach(System.out::println);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("update 3");

        Optional<ChoiceDomain> removedChoice = testContainer.choiceRepository.findById(1L);
        assertThat(removedChoice).isEmpty();
    }
}