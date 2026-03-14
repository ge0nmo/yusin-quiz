package com.cpa.yusin.quiz.dashboard.controller.dto.response;

public record DashboardExamContextResponse(
        long id,
        String name,
        int year,
        long problemCount,
        long questionCount,
        long unansweredQuestionCount,
        double lectureCoverageRate
) {
}
