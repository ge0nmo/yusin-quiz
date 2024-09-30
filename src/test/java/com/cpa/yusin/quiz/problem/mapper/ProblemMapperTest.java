package com.cpa.yusin.quiz.problem.mapper;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemMapperTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
    }


    @Test
    void toCreateResponse()
    {
        // given
        Problem problem = Problem.builder()
                .id(1L)
                .number(1)
                .exam(Exam.builder()
                        .build())
                .content("problem 1")
                .build();

        // when
        ProblemCreateResponse result = testContainer.problemMapper.toCreateResponse(problem, new ArrayList<>());

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo("problem 1");
    }
}