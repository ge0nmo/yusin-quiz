package com.cpa.yusin.quiz.choice.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChoiceRequest
{
    private Long id;
    private int number;
    private String content;
    private boolean isAnswer;
    private boolean isDeleted;

    public boolean isNew()
    {
        return id == null;
    }
}
