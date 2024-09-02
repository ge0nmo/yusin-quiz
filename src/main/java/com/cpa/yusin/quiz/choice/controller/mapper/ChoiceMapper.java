package com.cpa.yusin.quiz.choice.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;

public interface ChoiceMapper
{
    ChoiceDomain fromCreateRequestToDomain(ChoiceRequest request, ProblemDomain problemDomain);

    ChoiceCreateResponse toCreateResponse(ChoiceDomain domain);

    List<ChoiceCreateResponse> toCreateResponses(List<ChoiceDomain> domains);

    ChoiceResponse toResponse(ChoiceDomain domain);

    List<ChoiceResponse> toResponses(List<ChoiceDomain> domains);
}
