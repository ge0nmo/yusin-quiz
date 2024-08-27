package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemDomain
{
    private Long id;
    private String content;
    private int number;
    private ExamDomain exam;

    public ProblemDomain update(ProblemUpdateRequest request)
    {
        return ProblemDomain.builder()
                .id(this.id)
                .content(request.getContent())
                .number(request.getNumber())
                .exam(this.exam)
                .build();
    }
}
