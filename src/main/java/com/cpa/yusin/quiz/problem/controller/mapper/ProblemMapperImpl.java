package com.cpa.yusin.quiz.problem.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProblemMapperImpl implements ProblemMapper
{
    @Override
    public ProblemDomain toProblemDomain(ProblemRequest request, ExamDomain exam)
    {
        if(request == null || exam == null)
            return null;

        return ProblemDomain.builder()
                .content(request.getContent())
                .number(request.getNumber())
                .exam(exam)
                .build();
    }

    @Override
    public ProblemCreateResponse toCreateResponse(ProblemDomain domain, List<ChoiceCreateResponse> choiceCreateResponses)
    {
        if(domain == null)
            return null;

        return ProblemCreateResponse.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .number(domain.getNumber())
                .choices(choiceCreateResponses)
                .build();
    }

    @Override
    public ProblemDTO toProblemDTO(ProblemDomain domain)
    {
        if(domain == null)
            return null;

        return ProblemDTO.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .number(domain.getNumber())
                .build();
    }

    @Override
    public ProblemResponse toResponse(ProblemDomain domain, List<ChoiceResponse> choices)
    {
        if(domain == null)
            return null;

        if(choices == null || choices.isEmpty())
            choices = new ArrayList<>();

        return ProblemResponse.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .number(domain.getNumber())
                .choices(choices)
                .build();
    }

}
