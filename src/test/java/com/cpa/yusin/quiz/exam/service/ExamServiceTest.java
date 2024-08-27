package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExamServiceTest
{
    TestContainer testContainer;

    SubjectDomain physics;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        physics = testContainer.subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("Physics")
                .build());
    }

    @Test
    void save()
    {
        // given
        ExamCreateRequest request = ExamCreateRequest.builder()
                .name("2024 3차")
                .year(2024)
                .build();

        // when
        ExamCreateResponse result = testContainer.examService.save(1L, request);

        // then
        assertThat(result.getName()).isEqualTo("2024 3차");
        assertThat(result.getYear()).isEqualTo(2024);
    }

    @Test
    void update()
    {
        // given
        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("update exam")
                .year(2020)
                .build();

        // when
        testContainer.examService.update(1L, request);

        // then
        ExamDomain examDomain = testContainer.examRepository.findById(1L).orElseThrow();

        assertThat(examDomain.getName()).isEqualTo("update exam");
        assertThat(examDomain.getYear()).isEqualTo(2020);
    }

    @Test
    void deleteById()
    {
        // given
        ExamDomain shouldBeRemovedExam = testContainer.examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("2024 1차")
                .year(2024)
                .subjectDomain(physics)
                .build());

        ExamDomain shouldNotBeRemovedExam = testContainer.examRepository.save(ExamDomain.builder()
                .id(2L)
                .name("2024 2차")
                .year(2024)
                .subjectDomain(physics)
                .build());

        ProblemDomain shouldBeRemovedProblem = testContainer.problemRepository.save(ProblemDomain.builder()
                .id(1L)
                .content("content abc")
                .number(1)
                .exam(shouldBeRemovedExam)
                .build());

        ProblemDomain shouldNotBeRemovedProblem = testContainer.problemRepository.save(ProblemDomain.builder()
                .id(2L)
                .content("content abcdce")
                .number(1)
                .exam(shouldNotBeRemovedExam)
                .build());

        testContainer.choiceRepository.save(ChoiceDomain.builder()
                .id(1L)
                .content("choice 1")
                .number(1)
                .isAnswer(true)
                .problem(shouldBeRemovedProblem)
                .build());

        ChoiceDomain shouldNotBeRemovedChoice = testContainer.choiceRepository.save(ChoiceDomain.builder()
                .id(2L)
                .content("choice 1")
                .number(1)
                .isAnswer(true)
                .problem(shouldNotBeRemovedProblem)
                .build());

        long examId = 1L;

        // when
        boolean result = testContainer.examService.deleteById(examId);
        Optional<ExamDomain> removedExam = testContainer.examRepository.findById(examId);
        Optional<ProblemDomain> existingProblem = testContainer.problemRepository.findById(shouldNotBeRemovedProblem.getId());
        Optional<ChoiceDomain> existingChoice = testContainer.choiceRepository.findById(shouldNotBeRemovedChoice.getId());

        // then
        assertThat(result).isTrue();
        assertThat(removedExam).isEmpty();
        assertThat(existingProblem).isNotEmpty();
        assertThat(existingChoice).isNotEmpty();

    }
}
