package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

/**
 * 말문제 빠른 풀이의 subject 선택 화면 한 행에 필요한 카탈로그와 진행률 응답이다.
 * GET /api/v2/problem/word-practice/subjects에서 사용한다.
 */
public record WordPracticeSubjectResponse(
        Long subjectId,
        String subjectName,
        int solvedCount,
        int totalCount,
        int remainingCount,
        WordPracticeProgressStatus status
) {
}
