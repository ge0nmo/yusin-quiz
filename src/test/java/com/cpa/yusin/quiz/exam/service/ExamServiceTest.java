package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExamServiceTest extends MockSetup
{
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

}
