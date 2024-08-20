package com.cpa.yusin.quiz.member.domain;

import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.mock.FakeOAuth2UserInfo;
import com.cpa.yusin.quiz.mock.FakeUuidHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
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
        MemberDomain result = memberDomain.updateFromOauth2("James");

        // then
        assertThat(result.getUsername()).isEqualTo("James");
        assertThat(result.getEmail()).isEqualTo("test@naver.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getPlatform()).isEqualTo(Platform.GOOGLE);
        assertThat(result.getPassword()).isEqualTo("123123");

    }

}