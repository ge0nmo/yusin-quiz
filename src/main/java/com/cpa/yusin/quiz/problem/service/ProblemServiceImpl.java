package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
    public void saveOrUpdateProblem(long examId, List<ProblemRequest> requests)
    {
        ExamDomain exam = examService.findById(examId);
        List<Long> problemIdsToDelete = new ArrayList<>();
        Map<ProblemDomain, List<ChoiceRequest>> choiceUpdateMap = new HashMap<>();

        for(ProblemRequest request : requests)
        {
            if(request.isDeleted() && !request.isNew()){
                problemIdsToDelete.add(request.getId());
            } else{
                ProblemDomain problemDomain;
                if(request.isNew()){
                    problemDomain = problemMapper.toProblemDomain(request, exam);
                } else{
                    problemDomain = findById(request.getId());
                    problemDomain.update(examId, request);
                }
                problemDomain = problemRepository.save(problemDomain);
                choiceUpdateMap.put(problemDomain, request.getChoices());
            }
        }

        deleteProcess(problemIdsToDelete);
        choiceService.saveOrUpdate(choiceUpdateMap);
    }

    private void deleteProcess(List<Long> problemIds)
    {
        if(!problemIds.isEmpty()){
            choiceService.deleteAllByProblemIds(problemIds);
            problemRepository.deleteAllByIdInBatch(problemIds);
        }
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


    @Override
    public ProblemDTO getById(long id)
    {
        ProblemDomain problem = findById(id);

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problem.getId());

        return problemMapper.toProblemDTO(problem, choices);
    }

    @Override
    public ProblemDomain findById(long id)
    {
        return problemRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }
}
