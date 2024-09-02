package com.cpa.yusin.quiz.problem.mapper;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
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
        ProblemDomain problemDomain = ProblemDomain.builder()
                .id(1L)
                .number(1)
                .exam(ExamDomain.builder()
                        .build())
                .content("problem 1")
                .build();

        // when
        ProblemCreateResponse result = testContainer.problemMapper.toCreateResponse(problemDomain, new ArrayList<>());

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo("problem 1");
    }
}