package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.port.DeleteProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeleteProblemServiceImpl implements DeleteProblemService
{
    private final ProblemRepository problemRepository;

    @Transactional
    @Override
    public void execute(long id)
    {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

        problem.delete();

        problemRepository.save(problem);
    }
}
