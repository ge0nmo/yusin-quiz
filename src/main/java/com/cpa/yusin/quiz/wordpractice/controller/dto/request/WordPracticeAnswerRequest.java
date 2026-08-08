package com.cpa.yusin.quiz.wordpractice.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 말문제 빠른 풀이 화면이 현재 문제에 최초로 선택한 보기를 전달하는 요청이다. */
public record WordPracticeAnswerRequest(
        @NotNull @Positive Long problemId,
        @NotNull @Positive Long choiceId
) {
}
