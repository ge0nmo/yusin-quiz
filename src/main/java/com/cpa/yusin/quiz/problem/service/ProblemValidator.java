package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemValidator
{
    private final ProblemRepository problemRepository;

    public void validateUniqueProblemNumber(Long examId, int problemNumber)
    {
        if(problemRepository.existsByExamIdAndNumber(examId, problemNumber)){
            throw new ProblemException(ExceptionMessage.PROBLEM_NUMBER_EXISTS);
        }
    }
}
