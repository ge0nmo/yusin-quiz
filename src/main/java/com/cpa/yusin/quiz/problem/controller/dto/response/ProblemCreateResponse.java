package com.cpa.yusin.quiz.problem.controller.dto.response;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Builder
public class ProblemCreateResponse
{
    private final long id;
    private final String content;
    private final int number;

    private final List<ChoiceCreateResponse> choices;
}
