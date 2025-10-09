package com.cpa.yusin.quiz.common.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class ProblemChoiceFacade
{
    private final ProblemService problemService;
    private final ChoiceService choiceService;
    private final ChoiceMapper choiceMapper;

    public long saveChoice(long problemId, ChoiceCreateRequest request)
    {
        Problem problem = problemService.findById(problemId);
        Choice choice = choiceMapper.fromCreateRequestToChoice(request, problem);

        return choiceService.save(choice);
    }
}
