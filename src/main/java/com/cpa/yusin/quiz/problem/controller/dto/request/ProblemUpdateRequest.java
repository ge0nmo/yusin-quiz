package com.cpa.yusin.quiz.problem.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProblemUpdateRequest
{
    private int number;
    private String content;
}
