package com.cpa.yusin.quiz.dashboard.service.port;

public record DashboardSubjectContextProjection(
        long id,
        String name,
        long examCount,
        long problemCount
) {
}
