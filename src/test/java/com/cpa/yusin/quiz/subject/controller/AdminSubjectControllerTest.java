package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminSubjectControllerTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
    }

    @Test
    void save()
    {
        // given
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .name("English")
                .build();

        // when
        ResponseEntity<GlobalResponse<SubjectCreateResponse>> result = testContainer.adminSubjectController.save(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();

        SubjectCreateResponse response = result.getBody().getData();

        assertThat(response.getName()).isEqualTo("English");
        assertThat(response.getId()).isPositive();
    }

    @Test
    void update()
    {
        // given
        testContainer.subjectRepository.save(Subject.builder().id(1L).name("English").build());

        SubjectUpdateRequest request = SubjectUpdateRequest.builder()
                .name("Japanese")
                .build();

        // when
        ResponseEntity<GlobalResponse<SubjectDTO>> result = testContainer.adminSubjectController.update(1L, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        SubjectDTO response = result.getBody().getData();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Japanese");

    }



    @Test
    void deleteById()
    {
        // given
        testContainer.subjectRepository.save(Subject.builder().id(1L).name("Chemistry").build());

        // when
        ResponseEntity<Object> result = testContainer.adminSubjectController.deleteById(1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}