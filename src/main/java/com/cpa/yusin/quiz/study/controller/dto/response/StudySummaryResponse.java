package com.cpa.yusin.quiz.study.controller.dto.response;

import java.util.List;

public record StudySummaryResponse(
        int todaySolved,
        int currentStreak,
        int yearSolved,
        InProgressSessionResponse inProgress,
        List<InProgressSessionResponse> inProgressSessions
) {
    public StudySummaryResponse {
        inProgressSessions = inProgressSessions == null ? List.of() : List.copyOf(inProgressSessions);
    }

    /**
     * Backward-compatible constructor for callers that only know about the latest session.
     */
    public StudySummaryResponse(
            int todaySolved,
            int currentStreak,
            int yearSolved,
            InProgressSessionResponse inProgress
    ) {
        this(todaySolved, currentStreak, yearSolved, inProgress,
                inProgress == null ? List.of() : List.of(inProgress));
    }
}
