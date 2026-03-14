package com.cpa.yusin.quiz.dashboard.controller.dto.response;

public record DashboardTotalsResponse(
        long subjectCount,
        long examCount,
        long problemCount,
        long questionCount
) {
}
