package com.cpa.yusin.quiz.choice.controller.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Builder
@Getter
public class ChoiceCreateResponse
{
    private final long id;
    private final String content;
    private final int number;
    private boolean isAnswer;
}
