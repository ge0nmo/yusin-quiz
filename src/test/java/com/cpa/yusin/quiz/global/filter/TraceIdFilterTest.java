package com.cpa.yusin.quiz.global.filter;

import com.cpa.yusin.quiz.mock.FakeUuidHolder;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    @Test
    void shouldReuseIncomingRequestIdAndExposeItInResponseHeader() throws Exception {
        TraceIdFilter filter = new TraceIdFilter(new FakeUuidHolder("ignored-uuid-value"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/question/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Request-ID", "client-trace-id");

        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("traceId")).isEqualTo("client-trace-id");
            assertThat(MDC.get("memberId")).isEqualTo("anonymous");
            assertThat(((MockHttpServletResponse) res).getHeader("X-Request-ID")).isEqualTo("client-trace-id");
            ((MockHttpServletResponse) res).setStatus(204);
        });

        assertThat(response.getHeader("X-Request-ID")).isEqualTo("client-trace-id");
        assertThat(MDC.get("traceId")).isNull();
        assertThat(MDC.get("memberId")).isNull();
    }

    @Test
    void shouldGenerateTraceIdWhenRequestHeaderIsMissing() throws Exception {
        TraceIdFilter filter = new TraceIdFilter(new FakeUuidHolder("12345678-90ab-cdef"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/question/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get("traceId")).isEqualTo("12345678");
            assertThat(MDC.get("memberId")).isEqualTo("anonymous");
            assertThat(((MockHttpServletResponse) res).getHeader("X-Request-ID")).isEqualTo("12345678");
        });

        assertThat(response.getHeader("X-Request-ID")).isEqualTo("12345678");
        assertThat(MDC.get("traceId")).isNull();
        assertThat(MDC.get("memberId")).isNull();
    }
}
