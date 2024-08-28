package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
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

    public ProblemDomain update(long examId, ProblemUpdateRequest request)
    {
        validateExamId(examId);

        return ProblemDomain.builder()
                .id(this.id)
                .content(request.getContent())
                .number(request.getNumber())
                .exam(this.exam)
                .build();
    }

    private void validateExamId(long examId)
    {
        if(!exam.getId().equals(examId))
            throw new GlobalException(ExceptionMessage.EXAM_NOT_FOUND);

    }
}
