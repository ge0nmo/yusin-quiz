package com.cpa.yusin.quiz.dashboard.controller.dto.response;

import java.time.LocalDateTime;

public record DashboardPendingQuestionResponse(
        long id,
        String title,
        String username,
        LocalDateTime createdAt,
        int answerCount,
        long problemId
) {
}
