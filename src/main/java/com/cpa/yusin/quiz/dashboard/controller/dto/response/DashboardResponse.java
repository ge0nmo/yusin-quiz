package com.cpa.yusin.quiz.dashboard.controller.dto.response;

import java.util.List;

public record DashboardResponse(
        DashboardTotalsResponse totals,
        DashboardOperationsResponse operations,
        List<DashboardPendingQuestionResponse> pendingQuestions,
        DashboardContextResponse context
) {
}
