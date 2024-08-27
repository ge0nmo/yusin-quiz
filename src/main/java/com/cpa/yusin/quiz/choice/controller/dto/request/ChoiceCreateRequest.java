package com.cpa.yusin.quiz.choice.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChoiceCreateRequest
{
    private int number;
    private String content;
    private boolean isAnswer;
}
