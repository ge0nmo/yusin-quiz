package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.domain.Problem;
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
        testContainer.problemService.processSaveOrUpdate(request, biologyExam2.getId());

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
    void saveOrUpdate_whenUpdate()
    {
        // given
        long examId = biologyExam1.getId();
        Problem savedProblem = testContainer.problemRepository.save(Problem.builder()
                .id(10L)
                .number(10)
                .content("알맞은 것을 고르시오")
                .explanation("설명")
                .exam(biologyExam1)
                .build());

        testContainer.choiceRepository.saveAll(List.of(
                Choice.builder().id(10L).number(1).content("1").problem(savedProblem).isAnswer(false).build(),
                Choice.builder().id(11L).number(2).content("2").problem(savedProblem).isAnswer(true).build(),
                Choice.builder().id(12L).number(3).content("3").problem(savedProblem).isAnswer(true).build(),
                Choice.builder().id(13L).number(4).content("4").problem(savedProblem).isAnswer(false).build(),
                Choice.builder().id(14L).number(5).content("5").problem(savedProblem).isAnswer(false).build()
        ));
        List<ChoiceRequest> choiceRequests = List.of(
                ChoiceRequest.builder().id(10L).number(1).content("수정1").isAnswer(true).build(),
                ChoiceRequest.builder().id(10L).number(1).content("수정2").isAnswer(false).build(),
                ChoiceRequest.builder().id(10L).number(1).content("수정3").isAnswer(false).build(),
                ChoiceRequest.builder().id(10L).number(1).content("수정4").isAnswer(false).build(),
                ChoiceRequest.builder().id(10L).number(1).content("수정5").isAnswer(false).build()
        );

        ProblemRequest request = ProblemRequest.builder().id(10L).number(10).content("알맞을 것을 고르시오(수정)")
                .explanation("설명(수정)").choices(choiceRequests).build();

        // when

        ProblemDTO result = testContainer.problemService.processSaveOrUpdate(request, examId);

        // then
        assertThat(result.content()).isEqualTo("알맞을 것을 고르시오(수정)");
        assertThat(result.explanation()).isEqualTo("설명(수정)");

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
        GlobalResponse<List<ProblemDTO>> response = testContainer.problemService.getAllByExamId(examId);
        List<ProblemDTO> result = response.getData();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).choices()).hasSize(3);
        assertThat(result.get(1).choices()).isEmpty();
    }
}