package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProblemServiceImpl implements ProblemService
{
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final ExamService examService;
    private final ChoiceService choiceService;

    @Transactional
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

    @Transactional
    @Override
    public void update(List<ProblemUpdateRequest> requests)
    {
        List<Long> problemIdsToDelete = new ArrayList<>();
        List<ProblemDomain> problemsToUpdate = new ArrayList<>();
        Map<Long, List<ChoiceUpdateRequest>> choiceUpdateMap = new HashMap<>();

        for(ProblemUpdateRequest request : requests)
        {
            if(request.isDeleted()){
                problemIdsToDelete.add(request.getId());
            } else{
                ProblemDomain problemDomain = findById(request.getId());
                problemDomain = problemDomain.update(request);
                problemsToUpdate.add(problemDomain);

                choiceUpdateMap.put(problemDomain.getId(), request.getChoices());
            }
        }

        if(!problemIdsToDelete.isEmpty()){
            deleteAll(problemIdsToDelete);
        }

        if(!problemsToUpdate.isEmpty()){
            problemRepository.saveAll(problemsToUpdate);
        }

        for(Map.Entry<Long, List<ChoiceUpdateRequest>> entry : choiceUpdateMap.entrySet()){
            choiceService.update(entry.getKey(), entry.getValue());
        }

    }

    private void deleteAll(List<Long> problemIds)
    {
        choiceService.deleteAllByProblemIds(problemIds);
        problemRepository.deleteAllByIdInBatch(problemIds);
    }

    @Override
    public ProblemDomain getById(long id)
    {
        return null;
    }

    @Override
    public ProblemDomain findById(long id)
    {
        return problemRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }
}
