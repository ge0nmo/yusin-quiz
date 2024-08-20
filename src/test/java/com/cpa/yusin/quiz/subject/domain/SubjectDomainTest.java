package com.cpa.yusin.quiz.subject.domain;

import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SubjectDomainTest
{

    @Test
    void createDomainFromRequest()
    {
        // given
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .name("computer science")
                .build();

        // when
        SubjectDomain domain = SubjectDomain.from(request);

        // then
        assertThat(domain.getName()).isEqualTo("computer science");
    }
}