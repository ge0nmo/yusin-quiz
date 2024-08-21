package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceTest
{
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        testContainer.memberRepository
                .save(MemberDomain.builder()
                        .id(1L)
                        .email("test@gmail.com")
                        .password("aaaa")
                        .role(Role.USER)
                        .platform(Platform.HOME)
                        .build());

    }

    @Test
    void getById()
    {
        // given

        // when
        MemberDTO result = testContainer.memberService.getById(1L);

        // then
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getPlatform()).isEqualTo(Platform.HOME);
    }

    @Test
    void getById_ThrowError()
    {
        // given

        // when

        // then
        assertThatThrownBy(() -> testContainer.memberService.getById(2L))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void findById()
    {
        // given

        // when
        MemberDomain result = testContainer.memberService
                .findById(1L);

        // then
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getPlatform()).isEqualTo(Platform.HOME);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_ThrowError()
    {
        // given

        // when

        // then
        assertThatThrownBy(() -> testContainer.memberService.findById(2L))
                .isInstanceOf(GlobalException.class);
    }
}