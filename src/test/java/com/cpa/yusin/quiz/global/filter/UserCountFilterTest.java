package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.global.utils.VisitorWhiteListMatcher;
import com.cpa.yusin.quiz.visitor.service.VisitorService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCountFilterTest
{
    @Mock
    VisitorService visitorService;

    VisitorWhiteListMatcher whiteListMatcher;

    @Mock
    HttpServletResponse servletResponse;

    @Mock
    FilterChain filterChain;

    UserCountFilter userCountFilter;

    @BeforeEach
    void setUp()
    {
        whiteListMatcher = new VisitorWhiteListMatcher();
        userCountFilter = new UserCountFilter(visitorService, whiteListMatcher);
    }

    @Test
    void doFilterInternal() throws ServletException, IOException
    {
        // given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/v1/");

        // when
        userCountFilter.doFilterInternal(servletRequest, servletResponse, filterChain);

        // then
        verify(visitorService).saveInRedis(servletRequest);
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @DisplayName("should return true when the requestURI is in WHITE LIST")
    @ValueSource(strings = {"/css", "/js", "/images", "/favicon.ico"})
    @ParameterizedTest
    void shouldNotFilterWhenRequestURIIsInWhiteList(String requestUri) throws ServletException
    {
        // given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(requestUri);
        servletRequest.addHeader("User-Agent", "Chrome");

        // when
        boolean result = userCountFilter.shouldNotFilter(servletRequest);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("should return true when the User-Agent is in WHITE LIST")
    @ParameterizedTest
    @ValueSource(strings = {"curl", "prometheus", "zgrab", "github", "bot", "spider", "crawler"})
    void shouldNotFilterWhenUserAgentIsInWhiteList(String userAgent) throws ServletException
    {
        // given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/v1");
        servletRequest.addHeader("User-Agent", userAgent);

        // when
        boolean result = userCountFilter.shouldNotFilter(servletRequest);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("should return false when the path is not in WHITE LIST ")
    @Test
    void shouldFilterWhenStartWithApi() throws ServletException
    {
        // given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/v1/");
        servletRequest.addHeader("User-Agent", "Chrome");

        // when
        boolean result = userCountFilter.shouldNotFilter(servletRequest);

        // then
        assertThat(result).isFalse();
    }
}