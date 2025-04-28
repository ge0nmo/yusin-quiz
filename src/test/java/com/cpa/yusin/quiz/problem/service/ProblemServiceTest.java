package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemServiceTest extends MockSetup
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

        ProblemRequest request =
                ProblemRequest.builder().content("problem1").number(1).choices(choices1).build();

        // when
        testContainer.problemService.saveOrUpdate(request, biologyExam2.getId());

        // then
        List<Choice> choices = testContainer.choiceRepository.findAllByExamId(biologyExam2.getId());
        assertThat(choices).hasSize(5);
        assertThat(choices.getFirst().getContent()).isEqualTo("problem1 - choice1");
        assertThat(choices.getFirst().getNumber()).isEqualTo(1);
        assertThat(choices.getFirst().getIsAnswer()).isTrue();

        assertThat(choices.get(1).getContent()).isEqualTo("problem1 - choice2");
        assertThat(choices.get(1).getNumber()).isEqualTo(2);
        assertThat(choices.get(1).getIsAnswer()).isFalse();

        assertThat(choices.get(2).getContent()).isEqualTo("problem1 - choice3");
        assertThat(choices.get(2).getNumber()).isEqualTo(3);
        assertThat(choices.get(2).getIsAnswer()).isFalse();

        assertThat(choices.get(3).getContent()).isEqualTo("problem1 - choice4");
        assertThat(choices.get(3).getNumber()).isEqualTo(4);
        assertThat(choices.get(3).getIsAnswer()).isFalse();

        assertThat(choices.get(4).getContent()).isEqualTo("problem1 - choice5");
        assertThat(choices.get(4).getNumber()).isEqualTo(5);
        assertThat(choices.get(4).getIsAnswer()).isFalse();

        List<Problem> problems = testContainer.problemRepository.findAllByExamId(biologyExam2.getId());
        assertThat(problems).hasSize(1);

        assertThat(problems.getFirst().getContent()).isEqualTo("problem1");
        assertThat(problems.getFirst().getNumber()).isEqualTo(1);
    }



    @Test
    void getById()
    {
        // given

        // when

        // then
    }

    @Test
    void findById()
    {
        // given

        // when

        // then
    }

    @Test
    void getAllByExamId()
    {
        // given
        long examId = physicsExam1.getId();

        Problem biologyProblem = Problem.builder()
                .id(10L)
                .number(10)
                .content("biology")
                .exam(biologyExam1)
                .build();

        testContainer.choiceRepository.save(Choice.builder()
                .id(10L)
                .number(1)
                .content("biology content")
                .isAnswer(true)
                .problem(biologyProblem)
                .build());

        // when
        List<ProblemResponse> result = testContainer.problemService.getAllByExamId(examId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getChoices()).hasSize(3);
        assertThat(result.get(1).getChoices()).isEmpty();
    }
}