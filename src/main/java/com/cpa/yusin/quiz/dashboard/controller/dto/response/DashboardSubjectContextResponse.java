package com.cpa.yusin.quiz.dashboard.controller.dto.response;

public record DashboardSubjectContextResponse(
        long id,
        String name,
        long examCount,
        long problemCount
) {
}
