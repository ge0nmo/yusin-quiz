package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemDomain
{
    private Long id;
    private String content;
    private int number;
    private String explanation;
    private ExamDomain exam;

    public static ProblemDomain from()
    {
       return ProblemDomain.builder()

               .build();
    }
}
