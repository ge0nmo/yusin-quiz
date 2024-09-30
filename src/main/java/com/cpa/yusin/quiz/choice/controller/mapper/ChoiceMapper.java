package com.cpa.yusin.quiz.choice.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;

public interface ChoiceMapper
{
    Choice fromCreateRequestToDomain(ChoiceRequest request, Problem problem);

    ChoiceCreateResponse toCreateResponse(Choice domain);

    List<ChoiceCreateResponse> toCreateResponses(List<Choice> domains);

    ChoiceResponse toResponse(Choice domain);

    List<ChoiceResponse> toResponses(List<Choice> domains);
}
