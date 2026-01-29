package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;

import java.util.List;

public interface GetProblemV2Service
{
    ProblemV2Response getById(Long problemId);
    List<ProblemV2Response> getAllByExamId(Long examId);
}