package com.cpa.yusin.quiz.choice.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChoiceUpdateRequest
{
    private long id;
    private int number;
    private String content;
    private boolean isAnswer;
    private boolean isDeleted;
}
