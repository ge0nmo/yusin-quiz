package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;

import java.util.List;

/** 말문제 화면이 다음 제출 순서와 회차 진행률을 함께 그릴 때 사용하는 문제 묶음 응답이다. */
public record WordPracticeProblemBatchResponse(
        Long cycleId,
        int requestedCount,
        int returnedCount,
        boolean hasMore,
        String status,
        WordPracticeProgressResponse progress,
        List<ProblemV2Response> problems
) {
}
