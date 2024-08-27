package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProblemUpdateRequest
{
    private long id;
    private String content;
    private int number;
    private boolean isDeleted;
    List<ChoiceUpdateRequest> choices;
}
