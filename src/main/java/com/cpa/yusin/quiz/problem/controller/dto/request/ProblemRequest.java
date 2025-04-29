package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
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

    public boolean isNew()
    {
        return this.id == null || this.id.equals(-1L);
    }
}
