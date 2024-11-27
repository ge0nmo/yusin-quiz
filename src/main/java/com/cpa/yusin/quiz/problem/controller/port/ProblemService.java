package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;

public interface ProblemService
{
    void save(long examId, ProblemCreateRequest request);

    void update(long problemId, ProblemUpdateRequest request);

    void saveOrUpdateProblem(long examId, List<ProblemRequest> requests);

    List<ProblemResponse> getAllByExamId(long examId);

    ProblemDTO getById(long id);

    Problem findById(long id);
}
