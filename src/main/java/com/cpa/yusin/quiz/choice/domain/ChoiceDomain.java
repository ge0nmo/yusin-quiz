package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChoiceDomain
{
    private Long id;
    private int number;
    private String content;
    private boolean isAnswer;
    private ProblemDomain problem;

    public ChoiceDomain update(long problemId, ChoiceUpdateRequest request)
    {
        validateProblemId(problemId);

        return ChoiceDomain.builder()
                .id(this.id)
                .number(request.getNumber())
                .content(request.getContent())
                .isAnswer(request.isAnswer())
                .problem(this.problem)
                .build();
    }

    public void validateProblemId(long problemId)
    {
        if(!this.problem.getId().equals(problemId)){
            throw new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND);
        }
    }
}
