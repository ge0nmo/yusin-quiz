package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.Optional;

public interface ProblemService
{
    ProblemDomain save(long examId, ProblemCreateRequest problem);

    ProblemDomain getById(long id);

    Optional<ProblemDomain> findById(long id);
}
