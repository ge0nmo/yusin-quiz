package com.cpa.yusin.quiz.study.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExamFinishResponse {
    @Deprecated
    private final int finalScore;
    private final int correctCount;
    private final int totalCount;
    private final int answeredCount;
    private final int unansweredCount;
}
