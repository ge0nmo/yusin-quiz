package com.cpa.yusin.quiz.choice.controller.port;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;
import java.util.Map;

public interface ChoiceService
{
    void saveOrUpdate(Map<ProblemDomain, List<ChoiceRequest>> choiceMaps);

    ChoiceDomain findById(long id);

    List<ChoiceDomain> findAllByProblemId(long problemId);

    List<ChoiceResponse> getAllByProblemId(long problemId);

    Map<Long, List<ChoiceResponse>> findAllByExamId(long examId);

    void deleteAllByIds(List<Long> ids);

    void deleteAllByProblemId(long problemId);

    void deleteAllByProblemIds(List<Long> problemIds);
}
