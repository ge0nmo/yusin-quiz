package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceTest
{
    TestContainer testContainer;

    Member member;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();

        member = testContainer.memberRepository
                .save(Member.builder()
                        .id(1L)
                        .email("test@gmail.com")
                        .password("aaaa")
                        .username("Kim")
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
        Member result = testContainer.memberService
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

    @Test
    void getAllByKeyword()
    {
        // given
        testContainer.memberRepository.save(Member.builder().id(2L).email("abc@gmail.com").username("Harry")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(3L).email("zvde@gmail.com").username("Rachel")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(4L).email("aa222@gmail.com").username("John")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(5L).email("naver@gmail.com").username("Mike")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(6L).email("google@gmail.com").username("David")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(7L).email("mac@gmail.com").username("James")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(8L).email("pro@gmail.com").username("Pinkman")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(9L).email("ama@gmail.com").username("Tom")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());


        int page = 0;
        int size = 5;
        String keyword = "e";
        Pageable pageable = PageRequest.of(page, size);

        // when
        List<MemberDTO> result = testContainer.memberService.getAll(keyword, pageable).getContent();
        // then
        assertThat(result).hasSize(5);

        result.forEach(member -> {
            boolean contain = member.getUsername().toLowerCase().contains(keyword) || member.getEmail().toLowerCase().contains(keyword);
            assertThat(contain).isTrue();
        });

    }

    @Test
    void getAllByKeyword_keywordNull()
    {
        // given
        testContainer.memberRepository.save(Member.builder().id(2L).email("abc@gmail.com").username("Harry")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(3L).email("zvde@gmail.com").username("Rachel")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(4L).email("aa222@gmail.com").username("John")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(5L).email("naver@gmail.com").username("Mike")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(6L).email("google@gmail.com").username("David")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(7L).email("mac@gmail.com").username("James")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(8L).email("pro@gmail.com").username("Pinkman")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());

        testContainer.memberRepository.save(Member.builder().id(9L).email("ama@gmail.com").username("Tom")
                .password("aaaa").role(Role.USER).platform(Platform.HOME).build());


        int page = 0;
        int size = 8;
        String keyword = null;
        Pageable pageable = PageRequest.of(page, size);

        // when
        List<MemberDTO> result = testContainer.memberService.getAll(keyword, pageable).getContent();
        // then
        assertThat(result).hasSize(8);
    }

    @Test
    void update()
    {
        // given
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .username("update")
                .build();

        // when
        testContainer.memberService.update(member.getId(), request, member);

        // then
        Member updatedMember = testContainer.memberRepository.findById(member.getId()).orElseThrow();

        assertThat(updatedMember.getUsername()).isEqualTo("update");
    }

    @Test
    void update_when_memberId_is_different()
    {
        // given
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .username("update")
                .build();

        // when
        assertThatThrownBy(() -> testContainer.memberService.update(2L, request, member))
                .isInstanceOf(GlobalException.class);

        // then
    }
}