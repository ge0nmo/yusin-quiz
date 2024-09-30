package com.cpa.yusin.quiz.problem.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProblemMapper
{
    public Problem toProblemEntity(ProblemRequest request, Exam exam)
    {
        if(request == null || exam == null)
            return null;

        return Problem.builder()
                .content(request.getContent())
                .number(request.getNumber())
                .exam(exam)
                .build();
    }

    public ProblemCreateResponse toCreateResponse(Problem problem, List<ChoiceCreateResponse> choiceCreateResponses)
    {
        if(problem == null)
            return null;

        return ProblemCreateResponse.builder()
                .id(problem.getId())
                .content(problem.getContent())
                .number(problem.getNumber())
                .choices(choiceCreateResponses)
                .build();
    }

    public ProblemDTO toProblemDTO(Problem problem, List<ChoiceResponse> choices)
    {
        if(problem == null)
            return null;

        return ProblemDTO.builder()
                .id(problem.getId())
                .content(problem.getContent())
                .number(problem.getNumber())
                .choices(choices)
                .build();
    }

    public ProblemResponse toResponse(Problem problem, List<ChoiceResponse> choices)
    {
        if(problem == null)
            return null;

        if(choices == null || choices.isEmpty())
            choices = new ArrayList<>();

        return ProblemResponse.builder()
                .id(problem.getId())
                .content(problem.getContent())
                .number(problem.getNumber())
                .choices(choices)
                .build();
    }
}
