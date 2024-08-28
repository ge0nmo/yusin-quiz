package com.cpa.yusin.quiz.problem.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemDTO
{
    private final long id;
    private final String content;
    private final int number;

}