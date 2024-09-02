package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;

public interface ProblemService
{
    void saveOrUpdateProblem(long examId, List<ProblemRequest> requests);

    List<ProblemResponse> getAllByExamId(long examId);

    ProblemDTO getById(long id);

    ProblemDomain findById(long id);
}
