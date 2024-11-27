package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProblemUpdateRequest
{
    private String content;
    private int number;
    private List<ChoiceUpdateRequest> choices;
}
