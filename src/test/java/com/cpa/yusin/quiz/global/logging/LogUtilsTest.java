package com.cpa.yusin.quiz.global.logging;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogUtilsTest {

    @Test
    void shouldSummarizeGlobalResponseByPayloadType() {
        GlobalResponse<ProblemV2Response> response = GlobalResponse.success(problemResponse(1L));

        assertThat(LogUtils.toSimpleString(response))
                .isEqualTo("GlobalResponse(data=ProblemV2Response, pageInfo=absent)");
    }

    @Test
    void shouldSummarizeResponseEntityByStatusAndBodyType() {
        ResponseEntity<GlobalResponse<ProblemV2Response>> responseEntity =
                ResponseEntity.ok(GlobalResponse.success(problemResponse(1L)));

        assertThat(LogUtils.toSimpleString(responseEntity))
                .isEqualTo("ResponseEntity(status=200 OK, bodyType=GlobalResponse)");
    }

    @Test
    void shouldSummarizeCollectionPageAndSliceWithoutDumpingPayloads() {
        List<ProblemV2Response> responses = List.of(problemResponse(1L), problemResponse(2L));
        PageImpl<ProblemV2Response> page = new PageImpl<>(responses, PageRequest.of(0, 10), responses.size());
        SliceImpl<ProblemV2Response> slice = new SliceImpl<>(responses, PageRequest.of(0, 10), false);

        assertThat(LogUtils.toSimpleString(responses))
                .isEqualTo("List(size=2, elementType=ProblemV2Response)");
        assertThat(LogUtils.toSimpleString(page))
                .isEqualTo("Page(size=2, elementType=ProblemV2Response)");
        assertThat(LogUtils.toSimpleString(slice))
                .isEqualTo("Slice(size=2, elementType=ProblemV2Response)");
    }

    @Test
    void shouldKeepSimpleScalarValuesReadable() {
        assertThat(LogUtils.toSimpleString(42)).isEqualTo("42");
        assertThat(LogUtils.toSimpleString("short-text")).isEqualTo("short-text");
    }

    @Test
    void shouldTruncateLongStrings() {
        String longText = "a".repeat(1005);

        assertThat(LogUtils.toSimpleString(longText))
                .hasSize(1014)
                .endsWith("...(truncated)");
    }

    private ProblemV2Response problemResponse(Long id) {
        return ProblemV2Response.builder()
                .id(id)
                .number(id.intValue())
                .build();
    }
}
