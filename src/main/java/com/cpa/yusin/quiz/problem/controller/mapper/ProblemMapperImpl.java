package com.cpa.yusin.quiz.problem.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemMapperImpl implements ProblemMapper
{
    @Override
    public ProblemDomain toProblemDomain(ProblemCreateRequest request, ExamDomain exam)
    {
        return ProblemDomain.builder()
                .content(request.getContent())
                .number(request.getNumber())
                .exam(exam)
                .build();
    }

    @Override
    public ProblemCreateResponse toCreateResponse(ProblemDomain domain, List<ChoiceCreateResponse> choiceCreateResponses)
    {
        return ProblemCreateResponse.builder()
                .id(domain.getId())
                .content(domain.getContent())
                .number(domain.getNumber())
                .choices(choiceCreateResponses)
                .build();
    }

}
