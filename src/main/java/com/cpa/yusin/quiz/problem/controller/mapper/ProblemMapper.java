package com.cpa.yusin.quiz.problem.controller.mapper;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;

public interface ProblemMapper
{
    ProblemDomain toProblemDomain(ProblemRequest request, ExamDomain exam);

    ProblemCreateResponse toCreateResponse(ProblemDomain domain, List<ChoiceCreateResponse> choiceCreateResponses);

    ProblemDTO toProblemDTO(ProblemDomain domain);

    ProblemResponse toResponse(ProblemDomain domain, List<ChoiceResponse> choices);
}
