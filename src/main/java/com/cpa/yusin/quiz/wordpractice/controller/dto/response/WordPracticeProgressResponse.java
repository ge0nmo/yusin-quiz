package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

/**
 * 회차 시작·이어풀기 응답의 진행률 스냅샷이다.
 * 이후 문제 묶음·답안 API도 같은 필드 의미를 재사용한다.
 */
public record WordPracticeProgressResponse(
        int solvedCount,
        int correctCount,
        int incorrectCount,
        int totalCount,
        int remainingCount
) {
}
