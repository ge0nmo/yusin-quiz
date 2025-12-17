package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper; // 추가
import io.jsonwebtoken.ExpiredJwtException; // 추가
import io.jsonwebtoken.JwtException; // 추가
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType; // 추가
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Order(1)
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final MemberDetailsService memberDetailsService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper; // JSON 변환용 추가

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/api/admin/login",
            "/api/v1/oauth2/**",
            "/favicon.ico",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String requestPath = request.getServletPath();
        return EXCLUDE_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = getAuthorizationHeaderOrCookie(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 토큰 파싱 및 이메일 추출 (여기서 ExpiredJwtException 발생)
            String email = jwtService.extractSubject(token);

            if (!StringUtils.hasText(email) || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            MemberDetails memberDetails = memberDetailsService.loadUserByUsername(email);

            // 2. 토큰 유효성 검증
            if (!jwtService.isValidToken(token, memberDetails)) {
                filterChain.doFilter(request, response);
                return;
            }

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            setErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (JwtException e) {
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            setErrorResponse(response, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        } catch (Exception e) {
            log.error("보안 필터에서 예외가 발생했습니다: ", e);
            setErrorResponse(response, "AUTH_ERROR", "인증 처리 중 에러가 발생했습니다.");
        }
    }

    // [추가] 필터에서 직접 JSON 응답을 내려주기 위한 메소드
    private void setErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 에러
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("code", code);
        errorDetails.put("message", message);
        errorDetails.put("path", "SecurityFilter");

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    private boolean isValidHeader(String header) {
        return StringUtils.hasText(header) && header.startsWith("Bearer ");
    }

    private String getAuthorizationHeaderOrCookie(HttpServletRequest request) {
        String headerToken = request.getHeader(AUTHORIZATION);
        if (isValidHeader(headerToken)) {
            return headerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}