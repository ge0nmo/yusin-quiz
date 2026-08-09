package com.cpa.yusin.quiz.wordpractice.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 화면에 내려간 한 문제 묶음의 답안을 고정 순서 그대로 전달하는 요청이다. */
public record WordPracticeAnswerBatchRequest(
        @NotEmpty @Size(max = 5) List<@Valid WordPracticeAnswerRequest> answers
) {
}
