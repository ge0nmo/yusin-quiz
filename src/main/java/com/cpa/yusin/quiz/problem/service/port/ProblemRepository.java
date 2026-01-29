package com.cpa.yusin.quiz.problem.service.port;

import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository
{
    Problem save(Problem problem);

    List<Problem> findAll();

    List<Problem> findAllByExamId(long examId);

    Optional<Problem> findById(long id);

    boolean existsByExamIdAndNumber(Long examId, int number);
}
