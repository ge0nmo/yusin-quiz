package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

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

        List<ProblemRequest> request = List.of(
                ProblemRequest.builder().content("problem1").number(1).choices(choices1).build()
        );

        // when
        testContainer.problemService.saveOrUpdateProblem(biologyExam2.getId(), request);

        // then
        List<ChoiceDomain> choiceDomains = testContainer.choiceRepository.findAllByExamId(biologyExam2.getId());
        assertThat(choiceDomains).hasSize(5);
        assertThat(choiceDomains.getFirst().getContent()).isEqualTo("problem1 - choice1");
        assertThat(choiceDomains.getFirst().getNumber()).isEqualTo(1);
        assertThat(choiceDomains.getFirst().isAnswer()).isTrue();

        assertThat(choiceDomains.get(1).getContent()).isEqualTo("problem1 - choice2");
        assertThat(choiceDomains.get(1).getNumber()).isEqualTo(2);
        assertThat(choiceDomains.get(1).isAnswer()).isFalse();

        assertThat(choiceDomains.get(2).getContent()).isEqualTo("problem1 - choice3");
        assertThat(choiceDomains.get(2).getNumber()).isEqualTo(3);
        assertThat(choiceDomains.get(2).isAnswer()).isFalse();

        assertThat(choiceDomains.get(3).getContent()).isEqualTo("problem1 - choice4");
        assertThat(choiceDomains.get(3).getNumber()).isEqualTo(4);
        assertThat(choiceDomains.get(3).isAnswer()).isFalse();

        assertThat(choiceDomains.get(4).getContent()).isEqualTo("problem1 - choice5");
        assertThat(choiceDomains.get(4).getNumber()).isEqualTo(5);
        assertThat(choiceDomains.get(4).isAnswer()).isFalse();

        List<ProblemDomain> problems = testContainer.problemRepository.findAllByExamId(biologyExam2.getId());
        assertThat(problems).hasSize(1);

        assertThat(problems.getFirst().getContent()).isEqualTo("problem1");
        assertThat(problems.getFirst().getNumber()).isEqualTo(1);
    }

    @DisplayName("update when ids exist in the request")
    @Test
    void saveOrUpdate2()
    {
        // given
        List<ChoiceRequest> choices1 = List.of(
                ChoiceRequest.builder().id(1L).content("problem1 - choice1").number(1).isAnswer(false).build(),
                ChoiceRequest.builder().id(2L).content("problem1 - choice2").number(2).isAnswer(true).build(),
                ChoiceRequest.builder().id(3L).content("problem1 - choice3").number(3).isDeleted(true).isAnswer(false).build()
        );

        List<ProblemRequest> request = List.of(
                ProblemRequest.builder().id(physicsProblem1.getId()).content("problem1").number(1).choices(choices1).build()
        );

        // when
        testContainer.problemService.saveOrUpdateProblem(physicsExam1.getId(), request);

        // then
        List<ChoiceDomain> choiceDomains = testContainer.choiceRepository.findAllByExamId(physicsExam1.getId());

        assertThat(choiceDomains).hasSize(2);
        assertThat(choiceDomains.getFirst().getContent()).isEqualTo("problem1 - choice1");
        assertThat(choiceDomains.getFirst().getNumber()).isEqualTo(1);
        assertThat(choiceDomains.getFirst().isAnswer()).isFalse();
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

        ProblemDomain biologyProblem = ProblemDomain.builder()
                .id(10L)
                .number(10)
                .content("biology")
                .exam(biologyExam1)
                .build();

        testContainer.choiceRepository.save(ChoiceDomain.builder()
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