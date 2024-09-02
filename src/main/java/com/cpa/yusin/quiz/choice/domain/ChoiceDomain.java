package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
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

    public void update(long problemId, ChoiceRequest request)
    {
        validateProblemId(problemId);

        this.number = request.getNumber();
        this.content = request.getContent();
        this.isAnswer = request.isAnswer();
    }

    public void validateProblemId(long problemId)
    {
        if(!this.problem.getId().equals(problemId)){
            throw new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND);
        }
    }
}
