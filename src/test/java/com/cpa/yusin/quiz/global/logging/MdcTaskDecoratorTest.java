package com.cpa.yusin.quiz.global.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator taskDecorator = new MdcTaskDecorator();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldPropagateCallerMdcAndRestoreExecutorThreadContext() {
        MDC.put("traceId", "trace-123");
        MDC.put("memberId", "77");

        Runnable decorated = taskDecorator.decorate(() -> {
            assertThat(MDC.get("traceId")).isEqualTo("trace-123");
            assertThat(MDC.get("memberId")).isEqualTo("77");
            MDC.put("memberId", "changed-inside-task");
        });

        MDC.put("traceId", "executor-trace");
        MDC.put("memberId", "executor-user");

        decorated.run();

        assertThat(MDC.get("traceId")).isEqualTo("executor-trace");
        assertThat(MDC.get("memberId")).isEqualTo("executor-user");
    }

    @Test
    void shouldClearExecutorThreadContextWhenCallerHasNoMdc() {
        Runnable decorated = taskDecorator.decorate(() -> {
            assertThat(MDC.getCopyOfContextMap()).isNull();
        });

        MDC.put("traceId", "executor-trace");
        MDC.put("memberId", "executor-user");

        decorated.run();

        assertThat(MDC.get("traceId")).isEqualTo("executor-trace");
        assertThat(MDC.get("memberId")).isEqualTo("executor-user");
    }
}
