package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;

import java.util.List;

/** 원자적으로 저장되거나 멱등 재확인된 답안 묶음과 최신 회차 진행률이다. */
public record WordPracticeAnswerBatchResponse(
        List<WordPracticeAnswerResultResponse> answers,
        String status,
        WordPracticeProgressResponse progress
) {
    public static WordPracticeAnswerBatchResponse from(
            List<WordPracticeAnswer> answers,
            WordPracticeCycleResponse cycle
    ) {
        return new WordPracticeAnswerBatchResponse(
                answers.stream().map(WordPracticeAnswerResultResponse::from).toList(),
                cycle.status(),
                cycle.progress()
        );
    }
}
