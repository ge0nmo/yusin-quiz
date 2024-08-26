package com.cpa.yusin.quiz.choice.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class ChoiceMapperImpl implements ChoiceMapper
{
    @Override
    public ChoiceDomain fromCreateRequestToDomain(ChoiceCreateRequest request, ProblemDomain problemDomain)
    {
        if(request == null || problemDomain == null)
            return null;

        return ChoiceDomain.builder()
                .number(request.getNumber())
                .content(request.getContent())
                .isAnswer(request.isAnswer())
                .problem(problemDomain)
                .build();
    }

    @Override
    public List<ChoiceDomain> fromCreateRequestToDomain(List<ChoiceCreateRequest> requests, ProblemDomain problemDomain)
    {
        if(requests == null || requests.isEmpty())
            return Collections.emptyList();

        return requests.stream()
                .map(item -> this.fromCreateRequestToDomain(item, problemDomain))
                .toList();

    }

    @Override
    public ChoiceCreateResponse toCreateResponse(ChoiceDomain domain)
    {
        if(domain == null)
            return null;

        return ChoiceCreateResponse.builder()
                .id(domain.getId())
                .number(domain.getNumber())
                .content(domain.getContent())
                .isAnswer(domain.isAnswer())
                .build();
    }

    @Override
    public List<ChoiceCreateResponse> toCreateResponses(List<ChoiceDomain> domains)
    {
        if(domains == null || domains.isEmpty())
            return Collections.emptyList();

        return domains.stream()
                .map(this::toCreateResponse)
                .toList();
    }

}
