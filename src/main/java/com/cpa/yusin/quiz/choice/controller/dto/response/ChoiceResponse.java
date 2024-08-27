package com.cpa.yusin.quiz.choice.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChoiceResponse
{
    private final long id;
    private final int number;
    private final String content;
    private final boolean isAnswer;
}
