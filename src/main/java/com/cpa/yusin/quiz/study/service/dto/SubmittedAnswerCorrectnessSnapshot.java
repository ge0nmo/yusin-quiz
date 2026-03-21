package com.cpa.yusin.quiz.study.service.dto;

public record SubmittedAnswerCorrectnessSnapshot(
        Long problemId,
        Long choiceId,
        Boolean authoritativeCorrect
) {
}
