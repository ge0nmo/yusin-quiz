package com.cpa.yusin.quiz.study.controller.dto.response;

public record StudySummaryResponse(
        int todaySolved,
        int currentStreak,
        int yearSolved,
        InProgressSessionResponse inProgress
) {
}
