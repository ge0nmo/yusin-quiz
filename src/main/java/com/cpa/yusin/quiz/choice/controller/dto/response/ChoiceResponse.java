package com.cpa.yusin.quiz.choice.controller.dto.response;

import lombok.Builder;

@Builder
public record ChoiceResponse(long id, int number, String content, Boolean isAnswer) {
}
