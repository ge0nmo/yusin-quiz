package com.cpa.yusin.quiz.problem.controller.port;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;

public interface CreateProblemV2Service
{
    void saveOrUpdateV2(long examId, ProblemSaveV2Request request);
}
