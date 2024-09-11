package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProblemDomain
{
    private Long id;
    private String content;
    private int number;
    private ExamDomain exam;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void update(long examId, ProblemRequest request)
    {
        validateExamId(examId);

        this.content = request.getContent();
        this.number = request.getNumber();
    }

    private void validateExamId(long examId)
    {
        if(!exam.getId().equals(examId))
            throw new GlobalException(ExceptionMessage.EXAM_NOT_FOUND);

    }
}
