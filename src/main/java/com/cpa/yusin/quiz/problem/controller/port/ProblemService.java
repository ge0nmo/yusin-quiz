package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;
import java.util.Optional;

public interface ProblemService
{
    List<ProblemCreateResponse> save(long examId, List<ProblemCreateRequest> requests);

    ProblemDomain getById(long id);

    Optional<ProblemDomain> findById(long id);
}
