package com.cpa.yusin.quiz.study.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubmittedAnswerResponse {
    private Long problemId;
    private Long choiceId;
    @Getter(AccessLevel.NONE)
    private boolean isCorrect;

    @JsonProperty("isCorrect")
    public boolean isCorrect() {
        return isCorrect;
    }

    public static SubmittedAnswerResponse from(SubmittedAnswer answer) {
        return SubmittedAnswerResponse.builder()
                .problemId(answer.getProblemId())
                .choiceId(answer.getChoiceId())
                .isCorrect(answer.isCorrect())
                .build();
    }
}
