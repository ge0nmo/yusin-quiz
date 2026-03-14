package com.cpa.yusin.quiz.dashboard.controller.dto.response;

public record DashboardContextResponse(
        DashboardSubjectContextResponse subject,
        DashboardExamContextResponse exam
) {
}
