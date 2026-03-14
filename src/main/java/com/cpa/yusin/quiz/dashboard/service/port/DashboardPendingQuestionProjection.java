package com.cpa.yusin.quiz.dashboard.service.port;

import java.time.LocalDateTime;

public record DashboardPendingQuestionProjection(
        long id,
        String title,
        String username,
        LocalDateTime createdAt,
        int answerCount,
        long problemId
) {
}
