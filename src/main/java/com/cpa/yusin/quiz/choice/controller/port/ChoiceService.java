package com.cpa.yusin.quiz.choice.controller.port;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;
import java.util.Map;

public interface ChoiceService
{
    void save(Problem problem, List<ChoiceCreateRequest> requests);

    long save(Choice choice);

    List<Choice> saveOrUpdate(List<ChoiceRequest> requests, Problem problem);

    void update(long choiceId, ChoiceUpdateRequest request);

    void deleteById(long choiceId);

    Choice findById(long id);

    List<Choice> findAllByProblemId(long problemId);

    List<ChoiceResponse> getAllByProblemId(long problemId);

    Map<Long, List<ChoiceResponse>> findAllByExamId(long examId);

}
