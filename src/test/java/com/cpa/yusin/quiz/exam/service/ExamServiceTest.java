package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExamServiceTest
{
    TestContainer testContainer;

    Subject physics;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        physics = testContainer.subjectRepository.save(Subject.builder()
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
                .maxProblemCount(40)
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
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2024).maxProblemCount(40).build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("update exam")
                .year(2020)
                .maxProblemCount(40)
                .build();

        // when
        testContainer.examService.update(1L, request);

        // then
        Exam exam = testContainer.examRepository.findById(1L).orElseThrow();

        assertThat(exam.getName()).isEqualTo("update exam");
        assertThat(exam.getYear()).isEqualTo(2020);
    }

    @Test
    void deleteById()
    {
        // given
        Exam shouldBeRemovedExam = testContainer.examRepository.save(Exam.builder()
                .id(1L)
                .name("2024 1차")
                .year(2024)
                .subjectId(physics.getId())
                .build());

        Exam shouldNotBeRemovedExam = testContainer.examRepository.save(Exam.builder()
                .id(2L)
                .name("2024 2차")
                .year(2024)
                .subjectId(physics.getId())
                .build());

        Problem shouldBeRemovedProblem = testContainer.problemRepository.save(Problem.builder()
                .id(1L)
                .content("content abc")
                .number(1)
                .exam(shouldBeRemovedExam)
                .build());

        Problem shouldNotBeRemovedProblem = testContainer.problemRepository.save(Problem.builder()
                .id(2L)
                .content("content abcdce")
                .number(1)
                .exam(shouldNotBeRemovedExam)
                .build());

        testContainer.choiceRepository.save(Choice.builder()
                .id(1L)
                .content("choice 1")
                .number(1)
                .isAnswer(true)
                .problem(shouldBeRemovedProblem)
                .build());

        Choice shouldNotBeRemovedChoice = testContainer.choiceRepository.save(Choice.builder()
                .id(2L)
                .content("choice 1")
                .number(1)
                .isAnswer(true)
                .problem(shouldNotBeRemovedProblem)
                .build());
        List<Long> ids = List.of(1L);

        // when
        testContainer.examService.deleteById(ids);
        Optional<Exam> removedExam = testContainer.examRepository.findById(1L);
        Optional<Problem> existingProblem = testContainer.problemRepository.findById(shouldNotBeRemovedProblem.getId());
        Optional<Choice> existingChoice = testContainer.choiceRepository.findById(shouldNotBeRemovedChoice.getId());

        // then
        assertThat(removedExam).isEmpty();
        assertThat(existingProblem).isNotEmpty();
        assertThat(existingChoice).isNotEmpty();

    }
}
