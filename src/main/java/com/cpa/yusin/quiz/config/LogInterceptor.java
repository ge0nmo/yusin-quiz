package com.cpa.yusin.quiz.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor
{
    private static final String REQUEST_ID = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
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
}
