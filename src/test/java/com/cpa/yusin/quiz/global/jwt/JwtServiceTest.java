package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.config.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest
{
    TestContainer testContainer;
    static String ACCESSTOKEN;
    static String REFRESHTOKEN;
    MemberDetails memberDetails;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
        String email = "test@gmail.com";

        testContainer.memberRepository
                .save(Member.builder()
                        .id(1L)
                        .email(email)
                        .password("aaaa")
                        .role(Role.USER)
                        .platform(Platform.HOME)
                        .build());

        ACCESSTOKEN = testContainer.jwtService
                .createAccessToken(email);
        REFRESHTOKEN = testContainer.jwtService
                .createRefreshToken(email);

        memberDetails = testContainer.memberDetailsService.loadUserByUsername(email);
    }

    @Test
    void createAccessToken()
    {
        // given
        String email = "test@gmail.com";

        // when
        String result = testContainer.jwtService.createAccessToken(email);

        // then
        assertNotNull(result);
    }

    @Test
    void tokenType_shouldDistinguishAccessAndRefreshToken() {
        assertThat(testContainer.jwtService.isAccessToken(ACCESSTOKEN)).isTrue();
        assertThat(testContainer.jwtService.isRefreshToken(ACCESSTOKEN)).isFalse();
        assertThat(testContainer.jwtService.isRefreshToken(REFRESHTOKEN)).isTrue();
        assertThat(testContainer.jwtService.isAccessToken(REFRESHTOKEN)).isFalse();
    }

    @Test
    void isValidToken_shouldRejectRefreshTokenForAuthentication() {
        assertThat(testContainer.jwtService.isValidToken(ACCESSTOKEN, memberDetails)).isTrue();
        assertThat(testContainer.jwtService.isValidToken(REFRESHTOKEN, memberDetails)).isFalse();
    }

}
