package com.cpa.yusin.quiz.member.domain;

import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.mock.FakeOAuth2UserInfo;
import com.cpa.yusin.quiz.mock.FakeUuidHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

class MemberDomainTest
{
    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDomainWithMemberCreateRequest()
    {
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
        MemberDomain memberDomain = MemberDomain.fromHome(johnDoe, passwordEncoder);

        // then
        assertThat(memberDomain.getRole()).isEqualTo(Role.USER);
        assertThat(memberDomain.getPlatform()).isEqualTo(Platform.HOME);
        assertThat(memberDomain.getPassword()).isEqualTo(encodedPassword);
        assertThat(memberDomain.getEmail()).isEqualTo("test@naver.com");
        assertThat(memberDomain.getUsername()).isEqualTo("John Doe");
        assertThat(passwordEncoder.matches(rawPassword, memberDomain.getPassword())).isTrue();

    }


    @Test
    void createDomainWithOAuth2Attributes()
    {
        // given
        FakeOAuth2UserInfo oAuth2UserInfo = new FakeOAuth2UserInfo(new HashMap<>(), "google", "test user", "test@gmail.com", Platform.GOOGLE);
        FakeUuidHolder uuidHolder = new FakeUuidHolder("randomPassword");

        // when
        MemberDomain memberDomain = MemberDomain.fromOAuth2(oAuth2UserInfo, uuidHolder);

        // then
        assertThat(memberDomain.getRole()).isEqualTo(Role.USER);
        assertThat(memberDomain.getPlatform()).isEqualTo(Platform.GOOGLE);
        assertThat(memberDomain.getPassword()).isEqualTo("randomPassword");
        assertThat(memberDomain.getEmail()).isEqualTo("test@gmail.com");
        assertThat(memberDomain.getUsername()).isEqualTo("test user");
    }

    @Test
    void updateMemberDomainFromOAuthLogin()
    {
        // given
        MemberDomain memberDomain = MemberDomain.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        // when
        memberDomain.updateFromOauth2("James");

        // then
        assertThat(memberDomain.getUsername()).isEqualTo("James");
        assertThat(memberDomain.getEmail()).isEqualTo("test@naver.com");
        assertThat(memberDomain.getRole()).isEqualTo(Role.USER);
        assertThat(memberDomain.getPlatform()).isEqualTo(Platform.GOOGLE);
        assertThat(memberDomain.getPassword()).isEqualTo("123123");

    }

    @Test
    void update()
    {
        // given
        MemberDomain memberDomain = MemberDomain.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .subscriptionExpiredAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .username("Mike")
                .build();

        // when
        memberDomain.update(request);

        // then
        assertThat(memberDomain.getUsername()).isEqualTo("Mike");
    }

    @DisplayName("validateMember - ADMIN can pass validation")
    @Test
    void validateMember1()
    {
        // given
        MemberDomain memberDomain = MemberDomain.builder()
                .id(1L)
                .role(Role.ADMIN)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        // when and then
        assertDoesNotThrow(() -> memberDomain.validateMember(2L, memberDomain));
    }

    @DisplayName("validateMember - memberId should be the same if it's USER")
    @Test
    void validateMember2()
    {
        // given
        MemberDomain memberDomain = MemberDomain.builder()
                .id(1L)
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .password("123123")
                .email("test@naver.com")
                .username("John Doe")
                .build();

        // when and then
        assertThatThrownBy(() -> memberDomain.validateMember(2L, memberDomain))
                .isInstanceOf(GlobalException.class);
    }

}