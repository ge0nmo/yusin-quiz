package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectControllerTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
    }


    @Test
    void getAll()
    {
        // given
        testContainer.subjectRepository.save(Subject.builder().id(1L).name("Chemistry").build());
        testContainer.subjectRepository.save(Subject.builder().id(2L).name("Physics").build());
        testContainer.subjectRepository.save(Subject.builder().id(3L).name("Biology").build());
        testContainer.subjectRepository.save(Subject.builder().id(4L).name("Draft").status(SubjectStatus.DRAFT).build());
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        // when
        ResponseEntity<GlobalResponse<List<SubjectDTO>>> result = testContainer.subjectController.getAll(pageable);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        List<SubjectDTO> response = result.getBody().getData();

        assertThat(response)
                .isNotEmpty()
                .hasSize(3);

        assertThat(response.getFirst().getName()).isEqualTo("Biology");
        assertThat(response.get(1).getName()).isEqualTo("Chemistry");
        assertThat(response.get(2).getName()).isEqualTo("Physics");
    }
}
