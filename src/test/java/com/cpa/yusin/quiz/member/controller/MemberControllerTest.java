package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class MemberControllerTest
{
    TestContainer testContainer;
    Member member;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
        member = testContainer.memberRepository.save(Member.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build());
    }

    @Test
    void getById()
    {
        // given
        long id = 1L;

        // when
        ResponseEntity<GlobalResponse<MemberDTO>> result = testContainer.memberController.getById(id);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        MemberDTO response = result.getBody().getData();
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getPlatform()).isEqualTo(Platform.GOOGLE);
        assertThat(response.getUsername()).isEqualTo("John Doe");
    }

}