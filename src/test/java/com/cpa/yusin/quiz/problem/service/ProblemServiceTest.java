package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemServiceTest extends MockSetup
{
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
    void getAllByExamId() throws JsonProcessingException
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