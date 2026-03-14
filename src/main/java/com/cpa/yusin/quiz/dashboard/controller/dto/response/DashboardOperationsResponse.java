package com.cpa.yusin.quiz.dashboard.controller.dto.response;

public record DashboardOperationsResponse(
        long todayQuestionCount,
        long unansweredQuestionCount,
        long problemsWithoutLectureCount
) {
}
