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

class ExamControllerTest
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
    void getById()
    {
        // given
        ExamCreateResponse savedExam = testContainer.examService.save(1L, ExamCreateRequest.builder()
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .build());

        // when
        ResponseEntity<GlobalResponse<ExamDTO>> result = testContainer.examController.getById(savedExam.getId());

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isNotNull();

        ExamDTO response = result.getBody().getData();
        assertThat(response.getYear()).isEqualTo(2024);
        assertThat(response.getName()).isEqualTo("1차");
    }

    @Test
    void getAllExamBySubjectId()
    {
        // given
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2024).maxProblemCount(40).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("2차").year(2024).maxProblemCount(40).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("3차").year(2024).maxProblemCount(40).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2025).maxProblemCount(40).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("2차").year(2025).maxProblemCount(40).build());

        // when
        ResponseEntity<GlobalResponse<List<ExamDTO>>> result = testContainer.examController.getAllExamBySubjectIdAndYear(1L, 2024);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isNotNull();

        List<ExamDTO> response = result.getBody().getData();

        assertThat(response).hasSize(3);

        assertThat(response.get(0).getYear()).isEqualTo(2024);
        assertThat(response.get(0).getName()).isEqualTo("1차");

        assertThat(response.get(1).getYear()).isEqualTo(2024);
        assertThat(response.get(1).getName()).isEqualTo("2차");

        assertThat(response.get(2).getYear()).isEqualTo(2024);
        assertThat(response.get(2).getName()).isEqualTo("3차");

    }

}