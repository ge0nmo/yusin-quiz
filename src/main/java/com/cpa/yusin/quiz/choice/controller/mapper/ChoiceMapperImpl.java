package com.cpa.yusin.quiz.choice.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ChoiceMapperImpl implements ChoiceMapper
{
    @Override
    public Choice fromCreateRequestToDomain(ChoiceRequest request, Problem problem)
    {
        if(request == null || problem == null)
            return null;

        return Choice.builder()
                .number(request.getNumber())
                .content(request.getContent())
                .isAnswer(request.getIsAnswer())
                .problem(problem)
                .build();
    }


    @Override
    public ChoiceCreateResponse toCreateResponse(Choice domain)
    {
        if(domain == null)
            return null;

        return ChoiceCreateResponse.builder()
                .id(domain.getId())
                .number(domain.getNumber())
                .content(domain.getContent())
                .isAnswer(domain.getIsAnswer())
                .build();
    }

    @Override
    public List<ChoiceCreateResponse> toCreateResponses(List<Choice> domains)
    {
        if(domains == null || domains.isEmpty())
            return Collections.emptyList();

        return domains.stream()
                .map(this::toCreateResponse)
                .toList();
    }

    @Override
    public ChoiceResponse toResponse(Choice domain)
    {
        if(domain == null)
            return null;

        return ChoiceResponse.builder()
                .id(domain.getId())
                .number(domain.getNumber())
                .content(domain.getContent())
                .isAnswer(domain.getIsAnswer())
                .build();
    }

    @Override
    public List<ChoiceResponse> toResponses(List<Choice> domains)
    {
        if(domains == null || domains.isEmpty())
            return Collections.emptyList();

        return domains.stream()
                .map(this::toResponse)
                .toList();
    }

}
