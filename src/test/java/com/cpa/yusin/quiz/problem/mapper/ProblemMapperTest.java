package com.cpa.yusin.quiz.problem.mapper;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.infrastructure.Problem;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import com.cpa.yusin.quiz.subject.infrastructure.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProblemMapperTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
    }

    @Test
    void toProblemDomain()
    {
        // given
        ExamDomain examDomain = ExamDomain.builder()
                .id(3L)
                .name("2024 1차")
                .year(2024)
                .build();

        ProblemCreateRequest request = ProblemCreateRequest.builder()
                .number(1)
                .content("problem 1")
                .choiceCreateRequests(new ArrayList<>())
                .build();

        // when
        ProblemDomain problemDomain = testContainer.problemMapper.toProblemDomain(request, examDomain);

        // then
        assertThat(problemDomain.getContent()).isEqualTo("problem 1");
        assertThat(problemDomain.getNumber()).isEqualTo(1);
        assertThat(problemDomain.getId()).isNull();

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