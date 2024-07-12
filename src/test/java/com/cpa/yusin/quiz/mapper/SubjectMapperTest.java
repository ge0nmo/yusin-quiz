package com.cpa.yusin.quiz.mapper;

import com.cpa.yusin.quiz.domain.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;
import com.cpa.yusin.quiz.domain.entity.Subject;
import com.cpa.yusin.quiz.utils.DummyObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SubjectMapperTest extends DummyObject {

    @Autowired
    private SubjectMapper subjectMapper;

    Subject subject1;
    Subject subject2;
    Subject subject3;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        subject1 = mockSubject(1L, "회계학");
        subject2 = mockSubject(2L, "관세사");
        subject3 = mockSubject(3L, "보험계리사");
    }

    @Test
    void toSubjectEntity() {
        // given
        SubjectCreateRequest request = new SubjectCreateRequest();
        request.setName("회계학");

        // when
        Subject subjectEntity = subjectMapper.toSubjectEntity(request);

        // then
        assertThat(subjectEntity.getName()).isEqualTo("회계학");
        assertThat(subjectEntity.getId()).isNull();
    }

    @DisplayName("return null when subject request is null")
    @Test
    void toSubjectEntity2() {
        // given

        // when
        Subject subjectEntity = subjectMapper.toSubjectEntity(null);

        // then
        assertThat(subjectEntity).isNull();
    }


    @Test
    void toSubjectResponse() {
        // given

        // when
        SubjectResponse response = subjectMapper.toSubjectResponse(subject1);

        // then
        assertThat(response.getId()).isEqualTo(subject1.getId());
        assertThat(response.getName()).isEqualTo(subject1.getName());
    }

    @DisplayName("return null when subject is null")
    @Test
    void toSubjectResponse2() {
        // given

        // when
        SubjectResponse response = subjectMapper.toSubjectResponse(null);

        // then
        assertThat(response).isNull();
    }

    @Test
    void toSubjectResponseList() {
        // given
        List<Subject> subjectList = List.of(subject1, subject2, subject3);

        // when
        List<SubjectResponse> response = subjectMapper.toSubjectResponseList(subjectList);

        // then
        SubjectResponse response1 = response.get(0);
        SubjectResponse response2 = response.get(1);

        assertThat(response).hasSize(3);
        assertThat(response1.getId()).isEqualTo(subject1.getId());
        assertThat(response2.getId()).isEqualTo(subject2.getId());
    }

    @DisplayName("return empty list when subject list is null or empty")
    @Test
    void toSubjectResponseList2() {
        // given
        List<Subject> emptyList = new ArrayList<>();

        // when
        List<SubjectResponse> response1 = subjectMapper.toSubjectResponseList(emptyList);
        List<SubjectResponse> response2 = subjectMapper.toSubjectResponseList(null);

        // then

        assertThat(response1).isEmpty();
        assertThat(response1).hasSize(0);

        assertThat(response2).isEmpty();
        assertThat(response2).hasSize(0);
    }
}