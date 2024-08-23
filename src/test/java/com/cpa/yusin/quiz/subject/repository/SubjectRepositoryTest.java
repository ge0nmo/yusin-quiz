package com.cpa.yusin.quiz.subject.repository;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectRepositoryTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("Economy")
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
    void save()
    {
        // given
        SubjectDomain domain = SubjectDomain.builder()
                .id(10L)
                .name("Physics")
                .build();

        // when
        SubjectDomain result = testContainer.subjectRepository.save(domain);

        // then
        assertThat(result.getName()).isEqualTo("Physics");
    }

    @Test
    void findById()
    {
        // given
        long id = 1L;

        // when
        Optional<SubjectDomain> result = testContainer.subjectRepository.findById(id);

        // then
        assertThat(result.isPresent()).isTrue();

        SubjectDomain domain = result.get();

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getName()).isEqualTo("Economy");
    }

    @Test
    void findAll()
    {
        // given

        // when
        List<SubjectDomain> result = testContainer.subjectRepository.findAll();

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(3);
    }

    @Test
    void existsById_ReturnTrueIfExists()
    {
        // given
        long id = 1L;

        // when
        boolean result = testContainer.subjectRepository.existsById(id);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsById_ReturnFalseIfNotExists()
    {
        // given
        long id = 10L;

        // when
        boolean result = testContainer.subjectRepository.existsById(id);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void deleteById()
    {
        // given
        long id = 1L;

        // when
        testContainer.subjectRepository.deleteById(id);

        // then
        boolean result = testContainer.subjectRepository.existsById(1L);
        assertThat(result).isFalse();
    }
}