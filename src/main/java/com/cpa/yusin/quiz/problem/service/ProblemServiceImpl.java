package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProblemServiceImpl implements ProblemService
{
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final ExamService examService;
    private final ChoiceService choiceService;

    @Override
    public List<ProblemCreateResponse> save(long examId, List<ProblemCreateRequest> requests)
    {
        ExamDomain exam = examService.findById(examId);
        List<ProblemCreateResponse> responses = new ArrayList<>();

        for(ProblemCreateRequest request : requests)
        {
            ProblemDomain problemDomain = problemMapper.toProblemDomain(request, exam);
            problemDomain = problemRepository.save(problemDomain);

            List<ChoiceCreateResponse> choiceCreateResponses = choiceService.save(request.getChoiceCreateRequests(), problemDomain);

            ProblemCreateResponse createResponse = problemMapper.toCreateResponse(problemDomain, choiceCreateResponses);
            responses.add(createResponse);
        }

        return responses;
    }

    @Override
    public ProblemDomain getById(long id)
    {
        return null;
    }

    @Override
    public Optional<ProblemDomain> findById(long id)
    {
        return Optional.empty();
    }
}
