package com.cpa.yusin.quiz.subject.service;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        testContainer.subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("Economics")
                .build());

        testContainer.subjectRepository.save(SubjectDomain.builder()
                .id(2L)
                .name("Chemistry")
                .build());

        testContainer.subjectRepository.save(SubjectDomain.builder()
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
                .isInstanceOf(GlobalException.class);
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
        SubjectDomain result = testContainer.subjectService.findById(id);

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
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void findAllWithSortedByName()
    {
        // given

        // when
        List<SubjectDTO> result = testContainer.subjectService.getAll();

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
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void delete()
    {
        // given
        SubjectDomain physics = testContainer.subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("Physics")
                .build());

        ExamDomain physicsExam1 = testContainer.examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("2024 1차")
                .year(2024)
                .subjectDomain(physics)
                .build());

        ProblemDomain physicsProblem1 = testContainer.problemRepository.save(ProblemDomain.builder()
                .id(1L)
                .content("content abc")
                .number(1)
                .exam(physicsExam1)
                .build());

        testContainer.problemRepository.save(ProblemDomain.builder()
                .id(2L)
                .content("content zxc")
                .number(2)
                .exam(physicsExam1)
                .build());

        testContainer.choiceRepository.save(ChoiceDomain.builder()
                .id(1L)
                .content("choice 1")
                .number(1)
                .answer(true)
                .problem(physicsProblem1)
                .build());

        long subjectId = physics.getId();
        // when

        boolean result = testContainer.subjectService.deleteById(subjectId);

        // then
        Optional<SubjectDomain> removedSubject = testContainer.subjectRepository.findById(subjectId);
        Optional<ExamDomain> removedExam = testContainer.examRepository.findById(physicsExam1.getId());
        Optional<ProblemDomain> removedProblem1 = testContainer.problemRepository.findById(physicsProblem1.getId());
        Optional<ProblemDomain> removedProblem2 = testContainer.problemRepository.findById(2L);
        Optional<ChoiceDomain> removedChoice = testContainer.choiceRepository.findById(1L);

        assertThat(result).isTrue();

        assertThat(removedSubject).isEmpty();
        assertThat(removedExam).isEmpty();
        assertThat(removedProblem1).isEmpty();
        assertThat(removedProblem2).isEmpty();
        assertThat(removedChoice).isEmpty();


    }
}