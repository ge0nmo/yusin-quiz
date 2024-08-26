package com.cpa.yusin.quiz.choice.controller.port;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;

public interface ChoiceService
{
    List<ChoiceCreateResponse> save(List<ChoiceCreateRequest> choiceCreateRequests, ProblemDomain problem);

    void update(long problemId, List<ChoiceUpdateRequest> requests);

    ChoiceDomain findById(long id);

    void deleteAllByIds(List<Long> ids);

    void deleteAllByProblemId(long problemId);

    void deleteAllByProblemIds(List<Long> problemIds);
}
