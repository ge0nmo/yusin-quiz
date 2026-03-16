package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
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
                .build();

        // when
        ExamCreateResponse result = testContainer.examService.save(1L, request);

        // then
        assertThat(result.getName()).isEqualTo("2024 3차");
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(testContainer.examRepository.findById(1L).orElseThrow().getStatus()).isEqualTo(ExamStatus.DRAFT);
    }

    @Test
    void update()
    {
        // given
        testContainer.examService.save(1L, ExamCreateRequest.builder()
                .name("1차")
                .year(2024)
                .status(ExamStatus.PUBLISHED)
                .build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("completePayment exam")
                .year(2020)
                .build();

        // when
        testContainer.examService.update(1L, request);

        // then
        Exam exam = testContainer.examRepository.findById(1L).orElseThrow();

        assertThat(exam.getName()).isEqualTo("completePayment exam");
        assertThat(exam.getYear()).isEqualTo(2020);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.PUBLISHED);
    }

    @Test
    void getAllBySubjectId_shouldReturnOnlyPublishedExams() {
        testContainer.examService.saveAsAdmin(1L, ExamCreateRequest.builder()
                .name("공개 시험")
                .year(2024)
                .status(ExamStatus.PUBLISHED)
                .build());
        testContainer.examService.saveAsAdmin(1L, ExamCreateRequest.builder()
                .name("비공개 시험")
                .year(2024)
                .status(ExamStatus.DRAFT)
                .build());

        List<ExamDTO> result = testContainer.examService.getAllBySubjectId(1L, 2024);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("공개 시험");
        assertThat(result.getFirst().getStatus()).isEqualTo(ExamStatus.PUBLISHED);
    }

    @Test
    void getAllBySubjectIdForAdmin_shouldIncludeDraftAndPublishedExams() {
        testContainer.examService.saveAsAdmin(1L, ExamCreateRequest.builder()
                .name("공개 시험")
                .year(2024)
                .status(ExamStatus.PUBLISHED)
                .build());
        testContainer.examService.saveAsAdmin(1L, ExamCreateRequest.builder()
                .name("비공개 시험")
                .year(2024)
                .status(ExamStatus.DRAFT)
                .build());

        List<ExamDTO> result = testContainer.examService.getAllBySubjectIdForAdmin(1L, 2024);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ExamDTO::getStatus)
                .containsExactlyInAnyOrder(ExamStatus.PUBLISHED, ExamStatus.DRAFT);
    }

}
