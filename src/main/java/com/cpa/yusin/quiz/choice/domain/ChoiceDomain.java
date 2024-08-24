package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChoiceDomain
{
    private Long id;
    private int number;
    private String content;
    private boolean isAnswer;
    private ProblemDomain problem;

    public static ChoiceDomain from(ChoiceCreateRequest request, ProblemDomain problemDomain)
    {
        return ChoiceDomain.builder()
                .number(request.getNumber())
                .content(request.getContent())
                .isAnswer(request.isAnswer())
                .problem(problemDomain)
                .build();
    }
}
