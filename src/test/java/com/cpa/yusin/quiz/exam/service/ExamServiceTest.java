package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExamServiceTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        SubjectDomain physics = SubjectDomain.builder()
                .id(1L)
                .name("Physics")
                .build();

        SubjectDomain biology = SubjectDomain.builder()
                .id(2L)
                .name("Biology")
                .build();

        testContainer.subjectRepository.save(physics);
        testContainer.subjectRepository.save(biology);

        testContainer.examRepository.save(ExamDomain.builder()
                        .id(1L)
                        .name("2024 1차")
                        .year(2024)
                        .subjectDomain(physics)
                        .build());

        testContainer.examRepository.save(ExamDomain.builder()
                .id(2L)
                .name("2024 2차")
                .year(2024)
                .subjectDomain(physics)
                .build());


        testContainer.examRepository.save(ExamDomain.builder()
                .id(3L)
                .name("2024 1차")
                .year(2024)
                .subjectDomain(biology)
                .build());

        testContainer.examRepository.save(ExamDomain.builder()
                .id(4L)
                .name("2024 2차")
                .year(2024)
                .subjectDomain(biology)
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
}
