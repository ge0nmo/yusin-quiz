package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SecurityFilterTest {

    private final MemberDetailsService memberDetailsService = mock(MemberDetailsService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final SecurityFilter securityFilter =
            new SecurityFilter(memberDetailsService, jwtService, new ObjectMapper());

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPutAuthenticatedMemberIdIntoMdcWhenJwtAuthenticationSucceeds() throws Exception {
        Member member = Member.builder()
                .id(42L)
                .email("user@test.com")
                .password("encoded-password")
                .username("tester")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build();
        MemberDetails memberDetails = new MemberDetails(member, Map.of());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/bookmarks");
        request.setServletPath("/api/v1/bookmarks");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtService.extractSubject("valid-token")).willReturn("user@test.com");
        given(memberDetailsService.loadUserByUsername("user@test.com")).willReturn(memberDetails);
        given(jwtService.isValidToken("valid-token", memberDetails)).willReturn(true);

        securityFilter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("memberId")).isEqualTo("42");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(memberDetails);
        });
    }

    @Test
    void shouldKeepAnonymousMemberIdWhenRequestIsUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/problem/1");
        request.setServletPath("/api/v1/problem/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MDC.put("memberId", "anonymous");

        securityFilter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("memberId")).isEqualTo("anonymous");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        });
    }
}
