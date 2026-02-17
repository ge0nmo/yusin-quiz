package com.cpa.yusin.quiz.study.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExamAnswerResponse {
    private boolean success;
    private Boolean isCorrect; // Nullable (for Exam Mode)
    private String explanation; // Nullable (for Exam Mode)

    public static ExamAnswerResponse practice(boolean isCorrect, String explanation) {
        return ExamAnswerResponse.builder()
                .success(true)
                .isCorrect(isCorrect)
                .explanation(explanation)
                .build();
    }

    public static ExamAnswerResponse exam() {
        return ExamAnswerResponse.builder()
                .success(true)
                .isCorrect(null)
                .explanation(null)
                .build();
    }
}
