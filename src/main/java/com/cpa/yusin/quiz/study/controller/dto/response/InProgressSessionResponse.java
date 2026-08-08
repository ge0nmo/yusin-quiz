package com.cpa.yusin.quiz.study.controller.dto.response;

public record InProgressSessionResponse(
        Long sessionId,
        Long examId,
        String examName,
        String mode,
        int lastIndex,
        int answeredCount,
        int totalCount
) {
}
