package com.cpa.yusin.quiz.global.logging;

import org.slf4j.MDC;

import java.util.Map;

public final class LogMdcContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String MEMBER_ID_KEY = "memberId";
    public static final String ANONYMOUS_MEMBER_ID = "anonymous";

    private LogMdcContext() {
    }

    public static void putTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static void putAnonymousMemberId() {
        MDC.put(MEMBER_ID_KEY, ANONYMOUS_MEMBER_ID);
    }

    public static void putAuthenticatedMemberId(Long memberId) {
        if (memberId == null) {
            putAnonymousMemberId();
            return;
        }

        MDC.put(MEMBER_ID_KEY, String.valueOf(memberId));
    }

    public static void restoreRequestContext(String previousTraceId, String previousMemberId) {
        restoreKey(TRACE_ID_KEY, previousTraceId);
        restoreKey(MEMBER_ID_KEY, previousMemberId);
    }

    public static Map<String, String> copyContextMap() {
        return MDC.getCopyOfContextMap();
    }

    public static void replaceContextMap(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
            return;
        }

        MDC.setContextMap(contextMap);
    }

    private static void restoreKey(String key, String value) {
        if (value == null) {
            MDC.remove(key);
            return;
        }

        MDC.put(key, value);
    }
}
