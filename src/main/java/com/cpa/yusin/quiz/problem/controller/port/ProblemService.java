package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;

public interface ProblemService
{
    void save(long examId, ProblemCreateRequest request);

    ProblemDTO processSaveOrUpdate(ProblemRequest request, long examId);


    void update(long problemId, ProblemUpdateRequest request, long examId);

    void deleteProblem(long problemId, long examId);

    GlobalResponse<List<ProblemDTO>> getAllByExamId(long examId);

    ProblemDTO getById(long id);

    Problem findById(long id);
}
