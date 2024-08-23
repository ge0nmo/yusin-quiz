package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
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
    MemberDetails memberDetails;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
        String email = "test@gmail.com";

        testContainer.memberRepository
                .save(MemberDomain.builder()
                        .id(1L)
                        .email(email)
                        .password("aaaa")
                        .role(Role.USER)
                        .platform(Platform.HOME)
                        .build());

        ACCESSTOKEN = testContainer.jwtService
                .createAccessToken(email);

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

}