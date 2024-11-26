package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
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
    public void save(long examId, ProblemCreateRequest request)
    {
        Exam exam = examService.findById(examId);

        Problem problem = problemMapper.toProblemEntity(request, exam);

        problem = problemRepository.save(problem);
        choiceService.save(problem, request.getChoices());
    }


    @Transactional
    @Override
    public void saveOrUpdateProblem(long examId, List<ProblemRequest> requests)
    {
        /*Exam exam = examService.findById(examId);
        List<Long> problemIdsToDelete = new ArrayList<>();
        Map<Problem, List<ChoiceRequest>> choiceUpdateMap = new HashMap<>();

        for(ProblemRequest request : requests)
        {
            if(request.getIsDeleted() && !request.isNew()){
                problemIdsToDelete.add(request.getId());
            } else{
                Problem problem;
                if(request.isNew()){
                    problem = problemMapper.toProblemEntity(request, exam);
                } else{
                    problem = findById(request.getId());
                    problem.update(examId, request);
                }
                problem = problemRepository.save(problem);
                choiceUpdateMap.put(problem, request.getChoices());
            }
        }

        deleteProcess(problemIdsToDelete);
        choiceService.saveOrUpdate(choiceUpdateMap);*/
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
        List<Problem> problems = problemRepository.findAllByExamId(examId);
        Map<Long, List<ChoiceResponse>> choiceMap = choiceService.findAllByExamId(examId);

        return problems.stream()
                .map(problem -> problemMapper.toResponse(problem, choiceMap.get(problem.getId())))
                .toList();
    }


    @Override
    public ProblemDTO getById(long id)
    {
        Problem problem = findById(id);

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problem.getId());

        return problemMapper.toProblemDTO(problem, choices);
    }

    @Override
    public Problem findById(long id)
    {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }
}
