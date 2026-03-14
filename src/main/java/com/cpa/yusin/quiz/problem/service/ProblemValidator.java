package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProblemValidator
{
    private final ProblemRepository problemRepository;

    public void validateCreateNumber(long examId, int problemNumber) {
        if (problemRepository.existsByExamIdAndNumber(examId, problemNumber)) {
            throw new ProblemException(ExceptionMessage.PROBLEM_NUMBER_EXISTS);
        }
    }

    public void validateUpdateNumber(Problem problem, int requestedNumber) {
        if (problem.getNumber() == requestedNumber) {
            return;
        }

        validateCreateNumber(problem.getExam().getId(), requestedNumber);
    }

    public void validateBelongsToExam(Problem problem, long examId) {
        if (!problem.getExam().getId().equals(examId)) {
            throw new ProblemException(ExceptionMessage.INVALID_DATA);
        }
    }
}
