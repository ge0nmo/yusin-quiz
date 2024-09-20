package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminProblemControllerTest extends MockSetup
{
    @Test
    void saveOrUpdate()
    {
        // given
        List<ChoiceRequest> choices1 = List.of(
                ChoiceRequest.builder().content("problem1 - choice1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("problem1 - choice2").number(2).isAnswer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice3").number(3).isAnswer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice4").number(4).isAnswer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice5").number(5).isAnswer(false).build()

        );

        List<ProblemRequest> request = List.of(
                ProblemRequest.builder().content("problem1").number(1).choices(choices1).build()
        );

        // when
        ResponseEntity<GlobalResponse<List<ProblemResponse>>> result
                = testContainer.adminProblemController.saveOrUpdate(biologyExam2.getId(), request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        List<ProblemResponse> response = result.getBody().getData();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getContent()).isEqualTo("problem1");
        assertThat(response.getFirst().getNumber()).isEqualTo(1);

        List<ChoiceResponse> choices = response.getFirst().getChoices();
        assertThat(choices.getFirst().getContent()).isEqualTo("problem1 - choice1");
        assertThat(choices.getFirst().getNumber()).isEqualTo(1);
        assertThat(choices.getFirst().getIsAnswer()).isTrue();

        assertThat(choices.get(1).getContent()).isEqualTo("problem1 - choice2");
        assertThat(choices.get(1).getNumber()).isEqualTo(2);
        assertThat(choices.get(1).getIsAnswer()).isFalse();

    }


    @Test
    void getById()
    {
        // given

        // when

        // then
    }

    @Test
    void getAllByExamId()
    {
        // given

        // when

        // then
    }
}