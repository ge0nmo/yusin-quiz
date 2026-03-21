package com.cpa.yusin.quiz.global.security;

import com.cpa.yusin.quiz.global.filter.SecurityErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        SecurityErrorResponse errorResponse = SecurityErrorResponse.unauthorized(
                "AUTH_REQUIRED",
                "로그인이 필요합니다.",
                resolvePath(request));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String resolvePath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return servletPath == null || servletPath.isBlank() ? request.getRequestURI() : servletPath;
    }
}
