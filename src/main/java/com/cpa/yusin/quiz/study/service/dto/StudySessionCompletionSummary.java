package com.cpa.yusin.quiz.study.service.dto;

public record StudySessionCompletionSummary(
        int correctCount,
        int totalCount,
        int answeredCount,
        int unansweredCount
) {
    public int finalScore() {
        return correctCount;
    }
}
