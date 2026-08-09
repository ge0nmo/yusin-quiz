package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 배치 안에서 저장된 한 답안의 서버 판정 결과다. */
public record WordPracticeAnswerResultResponse(
        Long problemId,
        Long choiceId,
        @JsonProperty("isCorrect") boolean correct,
        int sequence
) {
    public static WordPracticeAnswerResultResponse from(WordPracticeAnswer answer) {
        return new WordPracticeAnswerResultResponse(
                answer.getProblemId(),
                answer.getChoiceId(),
                answer.isCorrect(),
                answer.getSequence()
        );
    }
}
