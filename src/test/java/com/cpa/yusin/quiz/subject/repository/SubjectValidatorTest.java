package com.cpa.yusin.quiz.subject.repository;

import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.mock.TestContainer;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubjectValidatorTest
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
    }

    @Test
    void shouldNotThrowErrorIfSubjectNameDoesNotExist()
    {
        // given
        String name = "Chemistry";

        // when

        // then
        testContainer.subjectValidator.validateName(name);
    }

    @Test
    void throwErrorIfSubjectNameExists()
    {
        // given
        String name = "Economics";

        // when

        // then
        assertThatThrownBy(() -> testContainer.subjectValidator.validateName(name))
                .isInstanceOf(GlobalException.class);
    }

}
