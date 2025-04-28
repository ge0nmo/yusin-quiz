package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProblemRequest
{
    private Long id;
    private String content;
    private int number;
    private String explanation;
    private List<ChoiceRequest> choices;
}
