package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * subject 선택 직후 생성되거나 재사용된 말문제 회차 응답이다.
 * issuedGuestToken은 서버가 최초 익명 참여자에게만 발급한 경우에만 포함된다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WordPracticeCycleResponse(
        Long cycleId,
        Long subjectId,
        int roundNumber,
        String status,
        String issuedGuestToken,
        WordPracticeProgressResponse progress
) {
}
