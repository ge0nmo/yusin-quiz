package com.cpa.yusin.quiz.study.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExamMode {
    PRACTICE, // 연습 모드 (정답 바로 확인 가능)
    EXAM; // 실전 모드 (제출 후 확인)

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExamMode from(String value) {
        if (value == null) {
            return null;
        }
        String upperValue = value.toUpperCase();
        if ("REAL".equals(upperValue)) {
            return EXAM;
        }
        for (ExamMode mode : values()) {
            if (mode.name().equals(upperValue)) {
                return mode;
            }
        }
        // 기본적으로 예외를 던지거나, null을 반환하여 @NotNull에서 잡히게 할 수 있음.
        // 여기서는 명확한 에러 메시지를 위해 IllegalArgumentException 던짐
        throw new IllegalArgumentException("Unknown ExamMode: " + value);
    }
}
