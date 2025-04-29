package com.cpa.yusin.quiz.problem.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.Problem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ProblemMapper
{
    private final ChoiceMapper choiceMapper;

    public Problem toProblemEntity(ProblemCreateRequest request, Exam exam)
    {
        if(request == null || exam == null)
            return null;

        return Problem.builder()
                .content(request.getContent())
                .number(request.getNumber())
                .explanation(request.getExplanation())
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
                .explanation(problem.getExplanation())
                .choices(choices)
                .build();
    }

    public ProblemDTO mapToProblemDTO(Problem problem, List<Choice> choices)
    {
        if(problem == null)
            return null;

        return ProblemDTO.builder()
                .id(problem.getId())
                .content(problem.getContent())
                .number(problem.getNumber())
                .explanation(problem.getExplanation())
                .choices(choiceMapper.toResponses(choices))
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
                .explanation(problem.getExplanation())
                .choices(choices)
                .build();
    }
}
