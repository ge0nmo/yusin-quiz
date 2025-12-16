package com.cpa.yusin.quiz.subject.repository;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        testContainer.subjectRepository.save(Subject.builder()
                .id(1L)
                .name("Economy")
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
    void save()
    {
        // given
        Subject domain = Subject.builder()
                .id(10L)
                .name("Physics")
                .build();

        // when
        Subject result = testContainer.subjectRepository.save(domain);

        // then
        assertThat(result.getName()).isEqualTo("Physics");
    }

    @Test
    void findById()
    {
        // given
        long id = 1L;

        // when
        Optional<Subject> result = testContainer.subjectRepository.findById(id);

        // then
        assertThat(result.isPresent()).isTrue();

        Subject domain = result.get();

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getName()).isEqualTo("Economy");
    }

}