package com.cpa.yusin.quiz.exam.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamDeleteRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminExamControllerTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.subjectRepository.save(Subject.builder()
                .id(1L)
                .name("Physics")
                .build());
    }

    @Test
    void save()
    {
        // given
        long subjectId = 1L;

        ExamCreateRequest request = ExamCreateRequest.builder()
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .build();

        // when
        ResponseEntity<GlobalResponse<ExamCreateResponse>> result = testContainer.adminExamController.save(subjectId, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));

        assertThat(result.getBody()).isNotNull();

        ExamCreateResponse response = result.getBody().getData();

        assertThat(response.getName()).isEqualTo("1차");
        assertThat(response.getYear()).isEqualTo(2024);
    }

    @Test
    void update()
    {
        // given
        ExamCreateResponse savedExam = testContainer.examService.save(1L, ExamCreateRequest.builder()
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("2차")
                .year(2023)
                .maxProblemCount(40)
                .build();

        // when
        ResponseEntity<GlobalResponse<ExamDTO>> result = testContainer.adminExamController.update(savedExam.getId(), request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isNotNull();

        ExamDTO response = result.getBody().getData();

        assertThat(response.getYear()).isEqualTo(2023);
        assertThat(response.getName()).isEqualTo("2차");

    }

    @Test
    void deleteById()
    {
        // given
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2024).maxProblemCount(40).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("2차").year(2024).maxProblemCount(40).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("3차").year(2024).maxProblemCount(40).build());
        List<Long> examIds = List.of(1L, 2L, 3L);
        ExamDeleteRequest request = ExamDeleteRequest.builder()
                .examIds(examIds)
                .build();
        // when
        ResponseEntity<GlobalResponse<String>> result = testContainer.adminExamController.deleteById(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}