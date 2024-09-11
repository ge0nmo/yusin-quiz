package com.cpa.yusin.quiz.exam.controller.admin;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamDeleteRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExamControllerAdminTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.subjectRepository.save(SubjectDomain.builder()
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
                .build();

        // when
        ResponseEntity<GlobalResponse<ExamCreateResponse>> result = testContainer.adminExamController.save(subjectId, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));

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
                .build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("2차")
                .year(2023)
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
    void getById()
    {
        // given
        ExamCreateResponse savedExam = testContainer.examService.save(1L, ExamCreateRequest.builder()
                .name("1차")
                .year(2024)
                .build());

        // when
        ResponseEntity<GlobalResponse<ExamDTO>> result = testContainer.adminExamController.getById(savedExam.getId());

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
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2024).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("2차").year(2024).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("3차").year(2024).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2025).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("2차").year(2025).build());

        // when
        ResponseEntity<GlobalResponse<List<ExamDTO>>> result = testContainer.adminExamController.getAllExamBySubjectId(1L, 2024);

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

    @Test
    void deleteById()
    {
        // given
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("1차").year(2024).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("2차").year(2024).build());
        testContainer.examService.save(1L, ExamCreateRequest.builder().name("3차").year(2024).build());
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