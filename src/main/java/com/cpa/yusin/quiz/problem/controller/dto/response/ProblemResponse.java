package com.cpa.yusin.quiz.problem.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProblemResponse
{
    private List<ProblemDTO> problems;
}
