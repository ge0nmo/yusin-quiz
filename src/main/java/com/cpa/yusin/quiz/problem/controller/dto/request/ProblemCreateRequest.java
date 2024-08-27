package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ProblemCreateRequest
{
    private String content;
    private int number;

    private List<ChoiceCreateRequest> choiceCreateRequests;
}
