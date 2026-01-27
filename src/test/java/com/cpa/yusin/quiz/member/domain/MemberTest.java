package com.cpa.yusin.quiz.member.domain;

import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

class MemberTest {
    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDomainWithMemberCreateRequest() {
        // given
        String rawPassword = "123123";
        String encodedPassword = "encodedPassword123";

        MemberCreateRequest johnDoe = MemberCreateRequest.builder()
                .email("test@naver.com")
                .password(rawPassword)
                .username("John Doe")
                .build();

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // when
        Member member = Member.fromHome(johnDoe.getEmail(), encodedPassword, johnDoe.getUsername());

        // then
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getPlatform()).isEqualTo(Platform.HOME);
        assertThat(member.getPassword()).isEqualTo(encodedPassword);
        assertThat(member.getEmail()).isEqualTo("test@naver.com");
        assertThat(member.getUsername()).isEqualTo("John Doe");
        assertThat(passwordEncoder.matches(rawPassword, member.getPassword())).isTrue();

    }

    @Test
    void updateMemberDomainFromOAuthLogin() {
        // given
        Member member = Member.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        // when
        member.updateFromOauth2("James");

        // then
        assertThat(member.getUsername()).isEqualTo("James");
        assertThat(member.getEmail()).isEqualTo("test@naver.com");
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getPlatform()).isEqualTo(Platform.GOOGLE);
        assertThat(member.getPassword()).isEqualTo("123123");

    }

    @Test
    void update() {
        // given
        Member member = Member.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .username("Mike")
                .build();

        // when
        member.update(request.getUsername());

        // then
        assertThat(member.getUsername()).isEqualTo("Mike");
    }

    @DisplayName("validateMember - ADMIN can pass validation")
    @Test
    void validateMember1() {
        // given
        Member member = Member.builder()
                .id(1L)
                .role(Role.ADMIN)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        // when and then
        assertDoesNotThrow(() -> member.validateMember(2L, member));
    }

    @DisplayName("validateMember - memberId should be the same if it's USER")
    @Test
    void validateMember2() {
        // given
        Member member = Member.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        // when and then
        assertThatThrownBy(() -> member.validateMember(2L, member))
                .isInstanceOf(MemberException.class);
    }

}