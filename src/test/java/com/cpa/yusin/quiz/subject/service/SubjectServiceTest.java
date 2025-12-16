package com.cpa.yusin.quiz.subject.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.SubjectException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubjectServiceTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.subjectRepository.save(Subject.builder()
                .id(1L)
                .name("Economics")
                .build());

        testContainer.subjectRepository.save(Subject.builder()
                .id(2L)
                .name("Chemistry")
                .build());

        testContainer.subjectRepository.save(Subject.builder()
                .id(3L)
                .name("English")
                .build());
    }

    @Test
    void save_WithUniqueName()
    {
        // given
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .name("Biology")
                .build();

        // when
        SubjectCreateResponse result = testContainer.subjectService.save(request);

        // then
        assertThat(result.getName()).isEqualTo("Biology");
    }

    @Test
    void save_ThrowErrorIfNameIsNotUnique()
    {
        // given
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .name("Economics")
                .build();

        // when


        // then
        assertThatThrownBy(() -> testContainer.subjectService.save(request))
                .isInstanceOf(SubjectException.class);
    }

    @Test
    void getById()
    {
        // given
        long id = 1L;

        // when
        SubjectDTO result = testContainer.subjectService.getById(id);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Economics");
    }

    @Test
    void findById()
    {
        // given
        long id = 1L;

        // when
        Subject result = testContainer.subjectService.findById(id);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Economics");
    }

    @Test
    void shouldThrowErrorIfSubjectDoesNotExist()
    {
        // given
        long id = 10L;

        // when

        // then
        assertThatThrownBy(() -> testContainer.subjectService.findById(id))
                .isInstanceOf(SubjectException.class);
    }

    @Test
    void findAllWithSortedByName()
    {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        // when
        List<SubjectDTO> result = testContainer.subjectService.getAll(pageable.previousOrFirst()).getContent();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Chemistry");
        assertThat(result.get(1).getName()).isEqualTo("Economics");
        assertThat(result.get(2).getName()).isEqualTo("English");
    }

    @Test
    void update_WhenIdIsTheSameAndNameDoesNotExist()
    {
        // given
        long id = 1L;
        SubjectUpdateRequest request = SubjectUpdateRequest.builder()
                .name("Microeconomics")
                .build();

        // when
        testContainer.subjectService.update(id, request);

        // then
        SubjectDTO result = testContainer.subjectService.getById(id);
        assertThat(result.getName()).isEqualTo("Microeconomics");
    }

    @Test
    void update_ThrowErrorWhenIdIsDifferentAndNameExists()
    {
        // given
        long id = 2L;
        SubjectUpdateRequest request = SubjectUpdateRequest.builder()
                .name("Economics")
                .build();

        // when

        // then
        assertThatThrownBy(() -> testContainer.subjectService.update(id, request))
                .isInstanceOf(SubjectException.class);
    }

}