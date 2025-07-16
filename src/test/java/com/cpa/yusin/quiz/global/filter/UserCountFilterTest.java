package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.visitor.service.VisitorService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCountFilterTest
{
    @Mock
    VisitorService visitorService;

    MockHttpServletRequest servletRequest;

    @Mock
    HttpServletResponse servletResponse;

    @Mock
    FilterChain filterChain;

    UserCountFilter userCountFilter;

    @BeforeEach
    void setUp()
    {
        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/css");

        userCountFilter = new UserCountFilter(visitorService);
    }

    @Test
    void doFilterInternal() throws ServletException, IOException
    {
        // given

        // when
        userCountFilter.doFilterInternal(servletRequest, servletResponse, filterChain);

        // then
        verify(visitorService).saveInRedis(servletRequest);
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @DisplayName("should return true when the path is in WHITE LIST")
    @Test
    void shouldNotFilterWhenStartWithCss() throws ServletException
    {
        // given

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

        // when
        boolean result = userCountFilter.shouldNotFilter(servletRequest);

        // then
        assertThat(result).isFalse();
    }
}