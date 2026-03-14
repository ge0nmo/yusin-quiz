package com.cpa.yusin.quiz.dashboard.service.port;

public record DashboardExamContextProjection(
        long id,
        String name,
        int year,
        long problemCount,
        long questionCount,
        long unansweredQuestionCount,
        long problemsWithLectureCount
) {
}
