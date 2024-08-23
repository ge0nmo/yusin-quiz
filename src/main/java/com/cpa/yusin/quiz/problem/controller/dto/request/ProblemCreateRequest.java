package com.cpa.yusin.quiz.problem.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProblemCreateRequest
{
    private String content;
    private int number;
}
