package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 답안 저장 또는 동일 답안 재시도 뒤 화면이 즉시 피드백과 진행률을 갱신하는 응답이다. */
public record WordPracticeAnswerResponse(
        Long problemId,
        Long choiceId,
        @JsonProperty("isCorrect") boolean correct,
        int sequence,
        String status,
        WordPracticeProgressResponse progress
) {
    /** 불변 답안 이력과 갱신된 회차 스냅샷을 HTTP 응답 형태로 바꾼다. */
    public static WordPracticeAnswerResponse from(WordPracticeAnswer answer, WordPracticeCycleResponse cycle) {
        return new WordPracticeAnswerResponse(answer.getProblemId(), answer.getChoiceId(), answer.isCorrect(),
                answer.getSequence(), cycle.status(), cycle.progress());
    }
}
