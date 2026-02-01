package com.cpa.yusin.quiz.global.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행
public class TraceIdFilter implements Filter
{

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // 외부(Nginx 등)에서 전달된 TraceID가 있으면 사용, 없으면 생성
        String requestTraceId = httpRequest.getHeader("X-Request-ID");
        String traceId = StringUtils.hasText(requestTraceId) ? requestTraceId : UUID.randomUUID().toString().substring(0, 8);

        MDC.put(TRACE_ID_KEY, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // 스레드 풀 재사용 시 오염 방지를 위해 반드시 초기화
        }
    }
}