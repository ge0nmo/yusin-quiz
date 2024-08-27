package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceUpdateRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceCreateResponse;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

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

        deleteProcess(problemIdsToDelete);
        updateProcess(problemsToUpdate);
        choiceService.update(choiceUpdateMap);
    }

    @Override
    public List<ProblemResponse> getAllByExamId(long examId)
    {
        List<ProblemDomain> problems = problemRepository.findAllByExamId(examId);
        Map<Long, List<ChoiceResponse>> choiceMap = choiceService.findAllByExamId(examId);

        return problems.stream()
                .map(problem -> problemMapper.toResponse(problem, choiceMap.get(problem.getId())))
                .toList();
    }

    private void updateProcess(List<ProblemDomain> domains)
    {
        if(!domains.isEmpty()){
            problemRepository.saveAll(domains);
        }
    }

    private void deleteProcess(List<Long> problemIds)
    {
        if(!problemIds.isEmpty()){
            choiceService.deleteAllByProblemIds(problemIds);
            problemRepository.deleteAllByIdInBatch(problemIds);
        }
    }

    @Override
    public ProblemDTO getById(long id)
    {
        return problemMapper.toProblemDTO(findById(id));
    }

    @Override
    public ProblemDomain findById(long id)
    {
        return problemRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }
}
