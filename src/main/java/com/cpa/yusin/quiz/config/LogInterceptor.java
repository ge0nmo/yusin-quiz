package com.cpa.yusin.quiz.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor
{
    List<String> excludePaths = List.of("/static/js", "/static/css", "/static/img", "/favicon.ico");

    private static final String REQUEST_ID = "requestId";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if(shouldNotFilter(request)) return true;

        String requestId = generateRequestId();
        MDC.put(REQUEST_ID, requestId);
        request.setAttribute("startTime", System.currentTimeMillis());
        log.info("Request Received: [ID: {}, Method: {}, URI: {}]", requestId, request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        if(shouldNotFilter(request)) return;

        long duration = calculateDuration(request);
        String requestId = MDC.get(REQUEST_ID);
        try{
            if (!Objects.isNull(ex))
                log.error("Request Failed: [ID: {} Status: {}, URI: {}, Duration: {}ms]", requestId, response.getStatus(), request.getRequestURI(), duration, ex);
            else
                log.info("Request Completed: [ID: {}, Status: {}, URI: {}, Duration: {}ms]", requestId, response.getStatus(), request.getRequestURI(), duration);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private long calculateDuration(HttpServletRequest request) {
        long startTime = (long) request.getAttribute("startTime");
        return System.currentTimeMillis() - startTime;
    }

    private boolean shouldNotFilter(HttpServletRequest request)
    {
        return excludePaths.stream().anyMatch(request.getRequestURI()::startsWith);
    }
}
