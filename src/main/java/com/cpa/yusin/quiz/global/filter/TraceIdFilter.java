package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.logging.LogMdcContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행
public class TraceIdFilter implements Filter
{
    private static final String TRACE_ID_HEADER = "X-Request-ID";
    private final UuidHolder uuidHolder;

    public TraceIdFilter(@Qualifier("systemUuidHolder") UuidHolder uuidHolder) {
        this.uuidHolder = uuidHolder;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 외부(Nginx 등)에서 전달된 TraceID가 있으면 사용, 없으면 생성
        String requestTraceId = httpRequest.getHeader(TRACE_ID_HEADER);
        String traceId = StringUtils.hasText(requestTraceId) ? requestTraceId : generateTraceId();
        String previousTraceId = MDC.get(LogMdcContext.TRACE_ID_KEY);
        String previousMemberId = MDC.get(LogMdcContext.MEMBER_ID_KEY);

        LogMdcContext.putTraceId(traceId);
        // 인증 전 단계의 로그도 같은 포맷으로 남기기 위해 기본값을 먼저 채운다.
        LogMdcContext.putAnonymousMemberId();
        httpResponse.setHeader(TRACE_ID_HEADER, traceId);

        long startNanos = System.nanoTime();

        try {
            chain.doFilter(request, response);
            logCompletedRequest(httpRequest, httpResponse, startNanos);
        } catch (IOException | ServletException | RuntimeException e) {
            long durationMs = elapsedMillis(startNanos);
            log.error("HTTP {} {} -> failed after {}ms | errorType={} | message={}",
                    httpRequest.getMethod(),
                    requestPath(httpRequest),
                    durationMs,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);
            throw e;
        } finally {
            LogMdcContext.restoreRequestContext(previousTraceId, previousMemberId);
        }
    }

    private String generateTraceId() {
        String normalized = uuidHolder.getRandom().replace("-", "");
        return normalized.substring(0, Math.min(8, normalized.length()));
    }

    private void logCompletedRequest(HttpServletRequest request, HttpServletResponse response, long startNanos) {
        long durationMs = elapsedMillis(startNanos);
        String message = String.format("HTTP %s %s -> status=%d | durationMs=%d",
                request.getMethod(),
                requestPath(request),
                response.getStatus(),
                durationMs);

        if (response.getStatus() >= 500) {
            log.error(message);
            return;
        }

        if (durationMs >= 3000) {
            log.warn(message);
            return;
        }

        log.info(message);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String requestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (!StringUtils.hasText(queryString)) {
            return request.getRequestURI();
        }

        return request.getRequestURI() + "?" + queryString;
    }
}
