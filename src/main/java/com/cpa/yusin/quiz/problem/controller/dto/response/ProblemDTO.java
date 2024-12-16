package com.cpa.yusin.quiz.problem.controller.dto.response;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProblemDTO
{
    private final long id;
    private final String content;
    private final int number;
    private final String explanation;

    private final List<ChoiceResponse> choices;
}
