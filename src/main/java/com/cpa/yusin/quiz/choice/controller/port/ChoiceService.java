package com.cpa.yusin.quiz.choice.controller.port;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;
import java.util.Map;

public interface ChoiceService
{
    void saveOrUpdate(Map<Problem, List<ChoiceRequest>> choiceMaps);

    Choice findById(long id);

    List<Choice> findAllByProblemId(long problemId);

    List<ChoiceResponse> getAllByProblemId(long problemId);

    Map<Long, List<ChoiceResponse>> findAllByExamId(long examId);

    void deleteAllByIds(List<Long> ids);

    void deleteAllByProblemId(long problemId);

    void deleteAllByProblemIds(List<Long> problemIds);
}
