package com.cpa.yusin.quiz.study.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExamModeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("JSON 문자열 'REAL'은 EXAM으로 역직렬화되어야 한다")
    void deserializeRealToExam() throws Exception {
        // given
        String json = "\"REAL\"";

        // when
        ExamMode result = objectMapper.readValue(json, ExamMode.class);

        // then
        assertThat(result).isEqualTo(ExamMode.EXAM);
    }

    @Test
    @DisplayName("JSON 문자열 'EXAM'은 EXAM으로 역직렬화되어야 한다")
    void deserializeExamToExam() throws Exception {
        // given
        String json = "\"EXAM\"";

        // when
        ExamMode result = objectMapper.readValue(json, ExamMode.class);

        // then
        assertThat(result).isEqualTo(ExamMode.EXAM);
    }

    @Test
    @DisplayName("JSON 문자열 'PRACTICE'는 PRACTICE로 역직렬화되어야 한다")
    void deserializePracticeToPractice() throws Exception {
        // given
        String json = "\"PRACTICE\"";

        // when
        ExamMode result = objectMapper.readValue(json, ExamMode.class);

        // then
        assertThat(result).isEqualTo(ExamMode.PRACTICE);
    }

    @Test
    @DisplayName("소문자 'real'도 EXAM으로 역직렬화되어야 한다")
    void deserializeLowerCaseRealToExam() throws Exception {
        // given
        String json = "\"real\"";

        // when
        ExamMode result = objectMapper.readValue(json, ExamMode.class);

        // then
        assertThat(result).isEqualTo(ExamMode.EXAM);
    }

    @Test
    @DisplayName("알 수 없는 값은 예외를 발생시켜야 한다")
    void throwExceptionForUnknownValue() {
        // given
        String json = "\"UNKNOWN\"";

        // when & then
        assertThatThrownBy(() -> objectMapper.readValue(json, ExamMode.class))
                .isInstanceOf(Exception.class);
    }
}
