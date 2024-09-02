package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ProblemRequest
{
    private Long id;
    private String content;
    private int number;
    private boolean isDeleted;
    private List<ChoiceRequest> choices;

    public boolean isNew()
    {
        return id == null;
    }
}
